package com.lankatrails.lankatrails_backend.rabbit.Security;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.access.AccessDeniedException;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class RateLimitingChannelInterceptor implements ChannelInterceptor {

    private final int permits;
    private final Duration duration;
    private final Map<String, RateLimiter> sessionLimiters = new ConcurrentHashMap<>();
    private final RateLimiterRegistry rateLimiterRegistry;

    public RateLimitingChannelInterceptor(int permits, Duration duration) {
        this.permits = permits;
        this.duration = duration;

        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(permits)
                .limitRefreshPeriod(duration)
                .timeoutDuration(Duration.ZERO) // Fail immediately if rate limit is exceeded
                .build();

        this.rateLimiterRegistry = RateLimiterRegistry.of(config);
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        String sessionId = accessor.getSessionId();

        if (sessionId == null) {
            // Should not happen for STOMP messages after connect, but as a safeguard.
            return message;
        }

        StompCommand command = accessor.getCommand();

        if (StompCommand.DISCONNECT.equals(command)) {
            // Clean up the rate limiter for the session to prevent memory leaks.
            sessionLimiters.remove(sessionId);
            log.debug("Removed rate limiter for disconnected session: {}", sessionId);
        } else if (StompCommand.SEND.equals(command) || StompCommand.SUBSCRIBE.equals(command)) {
            // Apply rate limiting to SEND and SUBSCRIBE commands.
            RateLimiter limiter = sessionLimiters.computeIfAbsent(
                    sessionId,
                    id -> rateLimiterRegistry.rateLimiter("stomp-limiter-" + id)
            );

            if (!limiter.acquirePermission()) {
                log.warn("Rate limit exceeded for session {}. Command: {}. Limit: {}/{}",
                        sessionId, command, this.permits, this.duration);
                // Throw an exception to reject the message.
                // The client should handle this error (e.g., by displaying a message).
                throw new AccessDeniedException("Too many requests. Please slow down.");
            }
        }
        return message;
    }
}