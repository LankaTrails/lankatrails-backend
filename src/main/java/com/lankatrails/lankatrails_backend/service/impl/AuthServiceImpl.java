package com.lankatrails.lankatrails_backend.service.impl;

import com.lankatrails.lankatrails_backend.dtos.request.*;
import com.lankatrails.lankatrails_backend.dtos.response.*;
import com.lankatrails.lankatrails_backend.exception.*;
import com.lankatrails.lankatrails_backend.factory.*;
import com.lankatrails.lankatrails_backend.model.*;
import com.lankatrails.lankatrails_backend.model.enums.UserRole;
import com.lankatrails.lankatrails_backend.repositories.*;
import com.lankatrails.lankatrails_backend.security.jwt.JwtUtils;
import com.lankatrails.lankatrails_backend.security.service.RefreshTokenRedisService;
import com.lankatrails.lankatrails_backend.security.service.UserDetailsImpl;
import com.lankatrails.lankatrails_backend.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private final UserFactory userFactory;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RefreshTokenRedisService refreshTokenRedisService;

    @Override
    @Transactional
    public RegistrationResponse registerTourist(TouristRegistrationRequest request) {
        log.info("Attempting tourist registration for email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        User user = userFactory.createUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        User savedUser = userRepository.save(user);
        log.info("Tourist registered successfully with ID: {}", savedUser.getUserId());

        return RegistrationResponse.builder()
                .userId(savedUser.getUserId())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .status(savedUser.getStatus())
                .emailVerified(savedUser.getEmailVerified())
                .build();
    }

    @Override
    @Transactional
    public RegistrationResponse registerProvider(ProviderRegistrationRequest request) {
        log.info("Attempting provider registration for email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        User user = userFactory.createUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        User savedUser = userRepository.save(user);
        log.info("Provider registered successfully with ID: {}", savedUser.getUserId());

        return RegistrationResponse.builder()
                .userId(savedUser.getUserId())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .status(savedUser.getStatus())
                .emailVerified(savedUser.getEmailVerified())
                .build();
    }

    @Override
    public LoginResponse authenticateUser(LoginRequest request) {
        log.info("Attempting login for email: {}", request.getEmail());

        // Fetch user by email to check if verified before authenticating
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + request.getEmail()));

        // Check if email is verified
        if (!user.getEmailVerified()) {
            log.warn("Login failed: Email not verified for {}", request.getEmail());
            throw new BadCredentialsException("Please verify your email before logging in.");
        }

        // Perform authentication (Spring Security)
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (AuthenticationException ex) {
            log.error("Authentication failed for {}: {}", request.getEmail(), ex.getMessage());
            throw new BadCredentialsException("Invalid email or password.");
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Generate tokens
        String jwt = jwtUtils.generateToken(userDetails);
        String refreshToken = jwtUtils.generateRefreshToken(userDetails);

        refreshTokenRedisService.storeToken(userDetails.getEmail(), refreshToken);

        log.info("User {} logged in successfully with role: {}", userDetails.getUsername(), userDetails.getAuthorities());

        // Build and return login response
        return LoginResponse.builder()
                .id(userDetails.getId())
                .email(userDetails.getUsername())
                .role(UserRole.valueOf(userDetails.getAuthorities().iterator().next().getAuthority()))
                .jwtToken(jwt)
                .refreshToken(refreshToken)
                .emailVerified(true)
                .build();
    }

    @Override
    public APIResponse<String> logoutUser(HttpServletRequest request) {
        log.info("Attempting logout for request: {}", request.getRequestURI());

        String jwtToken = jwtUtils.getJwtFromCookies(request);
        if (jwtToken == null) {
            log.warn("Logout failed: JWT token not found in cookies.");
            throw new UnauthorizedException("JWT token not found.");
        }

        String email = jwtUtils.getUserNameFromJwtToken(jwtToken);
        if (email == null) {
            log.warn("Logout failed: Email not found in JWT token.");
            throw new UnauthorizedException("Email not found in JWT token.");
        }

        // Delete refresh token from Redis
        refreshTokenRedisService.deleteToken(email);
        log.info("Refresh token deleted for email: {}", email);

        log.info("User {} logged out successfully.", email);
        return APIResponse.<String>builder()
                .success(true)
                .message("User logged out successfully.")
                .build();
    }

}