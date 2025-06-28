package com.lankatrails.lankatrails_backend.security.service;

import com.lankatrails.lankatrails_backend.model.User;
import com.lankatrails.lankatrails_backend.repositories.UserRepository;
import com.lankatrails.lankatrails_backend.factory.UserFactory;
import com.lankatrails.lankatrails_backend.security.jwt.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthUserService {

    private final UserRepository userRepository;
    private final UserFactory userFactory;
    private final JwtUtils jwtUtils;
    private final RefreshTokenRedisService refreshTokenRedisService;

    public User findOrCreateOAuthUser(String email, Map<String, Object> attributes) {
        return userRepository.findByEmail(email.toLowerCase())
                .orElseGet(() -> {
                    log.info("Creating new user for email: {}", email);
                    User newUser = userFactory.createOAuthUser(email, attributes);
                    newUser.setPassword("default");  // no actual use
                    return userRepository.save(newUser);
                });
    }
}
