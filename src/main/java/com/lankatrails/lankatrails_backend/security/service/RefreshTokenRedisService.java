package com.lankatrails.lankatrails_backend.security.service;

import com.lankatrails.lankatrails_backend.security.jwt.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RefreshTokenRedisService {

    private final RedisTemplate<String, String> redisTemplate;
    private final JwtUtils jwtUtils;

    private String getKey(String email) {
        return "refresh:" + email;
    }

    public void storeToken(String email, String refreshToken) {
        redisTemplate.opsForValue().set(getKey(email), refreshToken, Duration.ofMillis(jwtUtils.getJwtRefreshExpirationMs()));
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

