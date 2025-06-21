package com.lankatrails.lankatrails_backend.config;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class ApplicationRateLimiterConfig {

    @Bean
    public RateLimiterRegistry rateLimiterRegistry() {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(5)          // 5 requests
                .limitRefreshPeriod(Duration.ofMinutes(1))  // per 1 minute
                .timeoutDuration(Duration.ofSeconds(5))     // wait up to 5 seconds
                .build();

        return RateLimiterRegistry.of(config);
    }

    @Bean
    public RateLimiter loginRateLimiter(RateLimiterRegistry registry) {
        return registry.rateLimiter("loginRateLimiter");
    }
}