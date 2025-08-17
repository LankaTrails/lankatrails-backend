package com.lankatrails.lankatrails_backend.security.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RefreshTokenRedisService {

    private final RedisTemplate<String, String> redisTemplate;
    private final Duration refreshTokenTtl = Duration.ofDays(7);

    private String getKey(String email) {
        return "refresh:" + email;
    }

    public void storeToken(String email, String refreshToken) {
        redisTemplate.opsForValue().set(getKey(email), refreshToken, refreshTokenTtl);
    }

    public boolean validateRefreshToken(String email, String providedToken) {
        String stored = redisTemplate.opsForValue().get(getKey(email));
        return stored != null && stored.equals(providedToken);
    }

    public void deleteToken(String email) {
        redisTemplate.delete(getKey(email));
    }

    public void rotateToken(String email, String newToken) {
        storeToken(email, newToken);
    }
}

