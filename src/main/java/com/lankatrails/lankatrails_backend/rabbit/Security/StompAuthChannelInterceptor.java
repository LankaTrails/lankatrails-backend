package com.lankatrails.lankatrails_backend.rabbit.Security;

import com.lankatrails.lankatrails_backend.security.jwt.AuthTokenFilter;
import com.lankatrails.lankatrails_backend.security.service.UserDetailsImpl;
import com.lankatrails.lankatrails_backend.security.utils.AuthUtils;
import com.lankatrails.lankatrails_backend.service.ChatRoomService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final AuthTokenFilter authTokenFilter;
    private final ChatRoomService chatRoomService; // ASSUMPTION: This service exists.
    private final AuthUtils authUtils; // ASSUMPTION: This utility is refactored and efficient.
    private static final String USER_AUTHENTICATION_KEY = "userAuthentication";

    // Use constructor injection. @Lazy is used to break a potential circular dependency
    // between WebSecurityConfig -> WebSocketConfig -> StompAuthChannelInterceptor -> AuthTokenFilter -> WebSecurityConfig
    public StompAuthChannelInterceptor(@Lazy AuthTokenFilter authTokenFilter, ChatRoomService chatRoomService, AuthUtils authUtils) {
        this.authTokenFilter = authTokenFilter;
        this.chatRoomService = chatRoomService;
        this.authUtils = authUtils;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        StompCommand command = accessor.getCommand();

        if (StompCommand.CONNECT.equals(command)) {
            handleConnect(accessor);
        } else if (StompCommand.SUBSCRIBE.equals(command)) {
            handleSubscribe(accessor);
        }

        // For all commands after CONNECT, ensure the SecurityContext is populated
        // from the session attributes where we stored the Authentication object.
        populateSecurityContext(accessor);

        return message;
    }

    private void handleConnect(StompHeaderAccessor accessor) {
        String authHeader = accessor.getFirstNativeHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.error("Missing or invalid Authorization header on WebSocket CONNECT for session {}.", accessor.getSessionId());
            throw new AuthenticationCredentialsNotFoundException("Missing or invalid Authorization header.");
        }

        String token = authHeader.substring(7);
        try {
            Authentication authentication = authTokenFilter.authenticateUser(token);
            Map<String, Object> sessionAttributes = accessor.getSessionAttributes();

            if (sessionAttributes != null) {
                sessionAttributes.put(USER_AUTHENTICATION_KEY, authentication);
                accessor.setUser(authentication);
                log.info("User '{}' authenticated successfully for WebSocket session {}.", authentication.getName(), accessor.getSessionId());
            } else {
                log.warn("Cannot get session attributes to store user authentication for session id: {}", accessor.getSessionId());
            }
        } catch (ExpiredJwtException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException ex) {
            log.warn("WebSocket CONNECT failed due to invalid JWT for session {}: {}", accessor.getSessionId(), ex.getMessage());
            throw new AuthenticationCredentialsNotFoundException("Invalid JWT token.", ex);
        } catch (AuthenticationException ex) {
            log.error("WebSocket CONNECT authentication failed for session {}: {}", accessor.getSessionId(), ex.getMessage());
            throw ex;
        }
    }

    private void handleSubscribe(StompHeaderAccessor accessor) {
        Authentication authentication = getAuthentication(accessor);
        String destination = accessor.getDestination();

        if (!isUserAuthorizedForDestination(authentication, destination)) {
            log.warn("User '{}' attempted unauthorized subscription to destination '{}' for session {}",
                    (authentication != null ? authentication.getName() : "UNKNOWN"), destination, accessor.getSessionId());
            throw new AccessDeniedException("You are not authorized to subscribe to this channel.");
        }
        log.debug("User '{}' authorized for subscription to '{}'", authentication.getName(), destination);
    }

    private boolean isUserAuthorizedForDestination(Authentication authentication, String destination) {
        if (authentication == null || destination == null) {
            return false;
        }

        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            Long userId = userDetails.getId();

            // Check for chat room messages: /topic/room.{roomId}
            String roomPrefix = "/topic/room.";
            if (destination.startsWith(roomPrefix)) {
                Long roomId = Long.parseLong(destination.substring(roomPrefix.length()));
                return chatRoomService.isUserInRoom(userId, roomId);
            }

            // Check for typing indicators: /topic/typing.{roomId}
            String typingPrefix = "/topic/typing.";
            if (destination.startsWith(typingPrefix)) {
                Long roomId = Long.parseLong(destination.substring(typingPrefix.length()));
                return chatRoomService.isUserInRoom(userId, roomId);
            }

            // Check for user-specific error messages: /user/queue/errors
            // This should only be accessible by the authenticated user themselves
            if (destination.equals("/user/queue/errors")) {
                return true; // User can always subscribe to their own error queue
            }

            // Check for user-specific destinations: /user/{username}/queue/...
            // This pattern allows users to subscribe only to their own user-specific queues
            String userQueuePrefix = "/user/" + userDetails.getUsername() + "/";
            if (destination.startsWith(userQueuePrefix)) {
                return true; // User can subscribe to their own user-specific destinations
            }

            // Check for user ID-based destinations: /user/{userId}/queue/...
            String userIdQueuePrefix = "/user/" + userId + "/";
            if (destination.startsWith(userIdQueuePrefix)) {
                return true; // User can subscribe to their own user ID-specific destinations
            }

            log.warn("Unauthorized subscription attempt to destination: {} by user: {}", destination, userId);

        } catch (NumberFormatException | ClassCastException e) {
            log.error("Could not parse destination or get User details from authentication. Destination: {}", destination, e);
            return false;
        }

        // Deny by default if the destination does not match any expected pattern
        return false;
    }

    private void populateSecurityContext(StompHeaderAccessor accessor) {
        Authentication authentication = getAuthentication(accessor);
        if (authentication != null) {
            SecurityContextHolder.getContext().setAuthentication(authentication);
            accessor.setUser(authentication);
        }
    }

    private Authentication getAuthentication(StompHeaderAccessor accessor) {
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        if (sessionAttributes != null) {
            return (Authentication) sessionAttributes.get(USER_AUTHENTICATION_KEY);
        }
        return null;
    }
}