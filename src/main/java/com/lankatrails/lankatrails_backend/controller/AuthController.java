package com.lankatrails.lankatrails_backend.controller;


import com.lankatrails.lankatrails_backend.config.ApplicationRateLimiterConfig;
import com.lankatrails.lankatrails_backend.dtos.request.LoginRequest;
import com.lankatrails.lankatrails_backend.dtos.request.ProviderRegistrationRequest;
import com.lankatrails.lankatrails_backend.dtos.request.TouristRegistrationRequest;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.LoginResponse;
import com.lankatrails.lankatrails_backend.dtos.response.RegistrationResponse;
import com.lankatrails.lankatrails_backend.dtos.response.UserProfileDto;
import com.lankatrails.lankatrails_backend.security.jwt.JwtUtils;
import com.lankatrails.lankatrails_backend.security.service.RefreshTokenRedisService;
import com.lankatrails.lankatrails_backend.service.AuthService;
import io.github.resilience4j.ratelimiter.RateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtUtils jwtUtils;
    private final ApplicationRateLimiterConfig rateLimiterConfig;
    private final RateLimiter loginRateLimiter;
    private final RefreshTokenRedisService refreshTokenRedisService;

    @PostMapping("/signup/tourist")
    public ResponseEntity<APIResponse<RegistrationResponse>> registerTourist(
            @Valid @RequestBody TouristRegistrationRequest request) {
        APIResponse<RegistrationResponse> tourist = authService.registerTourist(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tourist);
    }

    @PostMapping("/signup/provider")
    public ResponseEntity<APIResponse<RegistrationResponse>> registerProvider(
            @Valid @RequestBody ProviderRegistrationRequest request) {
        APIResponse<RegistrationResponse> provider = authService.registerProvider(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(provider);
    }

    @PostMapping("/login")
    public ResponseEntity<APIResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpServletRequest) {

        if (!loginRateLimiter.acquirePermission()) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }

        APIResponse<LoginResponse> loginResponse = authService.authenticateUser(request, httpServletRequest);

        ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(loginResponse.getData().getJwtToken());
        ResponseCookie refreshCookie = jwtUtils.generateRefreshCookie(loginResponse.getData().getRefreshToken());

        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(loginResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<APIResponse<String>> logout(HttpServletRequest request) {
        APIResponse<String> responseMessage = authService.logoutUser(request);

        ResponseCookie jwtCookie = jwtUtils.getCleanJwtCookie();
        ResponseCookie refreshCookie = jwtUtils.getCleanRefreshCookie();

        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(responseMessage);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<APIResponse<LoginResponse>> refreshToken(HttpServletRequest request) {
        APIResponse<LoginResponse> loginResponse = authService.refreshToken(request);

        ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(loginResponse.getData().getJwtToken());
        ResponseCookie refreshCookie = jwtUtils.generateRefreshCookie(loginResponse.getData().getRefreshToken());

        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(loginResponse);
    }

    @GetMapping("/logged-user")
    public ResponseEntity<APIResponse<UserProfileDto>> getLoggedUserProfile(HttpServletRequest request) {
        APIResponse<UserProfileDto> userProfileResponse = authService.getLoggedUserProfile(request);

        return ResponseEntity.status(HttpStatus.OK)
                .body(userProfileResponse);
    }

}