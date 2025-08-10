package com.lankatrails.lankatrails_backend.rabbit.Security;

import com.lankatrails.lankatrails_backend.exception.BadRequestException;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;

import java.time.Duration;

public class RateLimitingChannelInterceptor implements ChannelInterceptor {

    private final RateLimiter limiter;

    public RateLimitingChannelInterceptor(int permits, Duration duration) {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(permits)
                .limitRefreshPeriod(duration)
                .timeoutDuration(Duration.ZERO) // Non-blocking
                .build();

        this.limiter = RateLimiter.of("stomp-rate-limiter", config);
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (!StompCommand.CONNECT.equals(accessor.getCommand())) {
            // Correct Resilience4j method
            if (!limiter.acquirePermission()) {
                throw new BadRequestException("Too many requests");
            }

        }
        return message;
    }
}