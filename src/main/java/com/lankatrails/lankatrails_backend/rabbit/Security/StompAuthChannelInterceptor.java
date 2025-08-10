package com.lankatrails.lankatrails_backend.rabbit.Security;

import com.lankatrails.lankatrails_backend.security.jwt.AuthTokenFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final AuthTokenFilter authTokenFilter;
    // A key to store the authenticated user in the WebSocket session attributes.
    private static final String USER_AUTHENTICATION_KEY = "userAuthentication";

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        // On CONNECT, we perform the one-time authentication and store the user
        // in the session attributes for the entire duration of the WebSocket session.
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.error("Missing or invalid Authorization header on WebSocket CONNECT for session {}.", accessor.getSessionId());
                throw new AuthenticationCredentialsNotFoundException("Missing or invalid Authorization header.");
            }

            String token = authHeader.substring(7);
            try {
                Authentication authentication = authTokenFilter.authenticateUser(token);

                // The key step: Store the Authentication object in the session attributes.
                Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
                if (sessionAttributes != null) {
                    sessionAttributes.put(USER_AUTHENTICATION_KEY, authentication);
                } else {
                    log.warn("Cannot get session attributes to store user authentication for session id: {}", accessor.getSessionId());
                }

                // Also set the user on the current CONNECT message's accessor.
                accessor.setUser(authentication);
                log.info("User '{}' authenticated successfully for WebSocket session {}.", authentication.getName(), accessor.getSessionId());

            } catch (Exception e) {
                log.error("WebSocket authentication failed for token: {}", token, e);
                throw new AuthenticationCredentialsNotFoundException("WebSocket authentication failed.", e);
            }
        }

        // For all messages (SEND, SUBSCRIBE, etc.), we retrieve the stored authentication
        // from the session attributes and populate the SecurityContext.
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        if (sessionAttributes != null) {
            Authentication authentication = (Authentication) sessionAttributes.get(USER_AUTHENTICATION_KEY);
            if (authentication != null) {
                // Set the user on the current message's accessor and, most importantly,
                // in the SecurityContextHolder for the current thread.
                accessor.setUser(authentication);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        return message;
    }
}
