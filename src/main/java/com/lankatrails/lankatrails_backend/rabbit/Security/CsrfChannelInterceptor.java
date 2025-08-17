//package com.lankatrails.lankatrails_backend.rabbit.Security;
//
//import com.lankatrails.lankatrails_backend.exception.UnauthorizedException;
//import org.springframework.messaging.Message;
//import org.springframework.messaging.MessageChannel;
//import org.springframework.messaging.simp.stomp.StompCommand;
//import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
//import org.springframework.messaging.support.ChannelInterceptor;
//import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
//import org.springframework.security.web.csrf.CsrfTokenRepository;
//import org.springframework.stereotype.Component;
//
//@Component
//public class CsrfChannelInterceptor implements ChannelInterceptor {
//
//    private final CsrfTokenRepository csrfTokenRepository;
//
//    public CsrfChannelInterceptor() {
//        this.csrfTokenRepository = new CookieCsrfTokenRepository();
//    }
//
//    @Override
//    public Message<?> preSend(Message<?> message, MessageChannel channel) {
//        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
//
//        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
//            String csrfToken = accessor.getFirstNativeHeader("X-CSRF-TOKEN");
//            if (!validateCsrfToken(csrfToken)) {
//                throw new UnauthorizedException("Invalid CSRF token");
//            }
//        }
//        return message;
//    }
//
//    private boolean validateCsrfToken(String token) {
//        // Implement CSRF validation logic
//        return true; // Placeholder
//    }
//}
