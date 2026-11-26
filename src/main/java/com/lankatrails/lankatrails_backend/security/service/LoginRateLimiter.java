package com.lankatrails.lankatrails_backend.security.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Locale;

/**
 * Fixed-window login throttling backed by Redis, so limits hold across backend instances.
 * <p>
 * Two buckets per window: one per client IP and account (stops password guessing against one user)
 * and a looser one per client IP (stops spraying many accounts). Different users signing in from
 * the same machine, as in local testing, each get their own account bucket.
 */
@Slf4j
@Service
public class LoginRateLimiter {

    private final RedisTemplate<String, String> redisTemplate;
    private final int perAccount;
    private final int perIp;
    private final Duration window;

    public LoginRateLimiter(RedisTemplate<String, String> redisTemplate,
                            @Value("${app.security.login-rate-limit.per-account:5}") int perAccount,
                            @Value("${app.security.login-rate-limit.per-ip:30}") int perIp,
                            @Value("${app.security.login-rate-limit.window-seconds:60}") long windowSeconds) {
        this.redisTemplate = redisTemplate;
        this.perAccount = perAccount;
        this.perIp = perIp;
        this.window = Duration.ofSeconds(windowSeconds);
    }

    /** Records an attempt and returns whether it is allowed. */
    public boolean tryAcquire(String clientIp, String email) {
        String ip = clientIp == null ? "unknown" : clientIp;
        String account = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        try {
            long ipCount = increment("login-rate:ip:" + ip);
            long accountCount = increment("login-rate:account:" + ip + ":" + account);
            return ipCount <= perIp && accountCount <= perAccount;
        } catch (DataAccessException e) {
            // Fail open: refusing every login while Redis is down would lock everyone out
            log.warn("Login rate limiting skipped, Redis unavailable: {}", e.getMessage());
            return true;
        }
    }

    public long windowSeconds() {
        return window.toSeconds();
    }

    private long increment(String key) {
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redisTemplate.expire(key, window);
        }
        return count == null ? 0L : count;
    }
}
