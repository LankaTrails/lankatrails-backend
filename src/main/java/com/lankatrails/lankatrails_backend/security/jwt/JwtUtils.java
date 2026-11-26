package com.lankatrails.lankatrails_backend.security.jwt;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.WebUtils;

import com.lankatrails.lankatrails_backend.exception.UnauthorizedException;
import com.lankatrails.lankatrails_backend.model.User;
import com.lankatrails.lankatrails_backend.security.service.UserDetailsImpl;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JwtUtils {
    /** Every token carries a "type" claim so one kind can never be used as another. */
    public static final String CLAIM_TYPE = "type";
    public static final String TYPE_ACCESS = "ACCESS";
    public static final String TYPE_REFRESH = "REFRESH";
    public static final String TYPE_EMAIL_VERIFICATION = "EMAIL_VERIFICATION";

    @Value("${spring.app.jwtSecret}")
    private String jwtSecret;

    @Value("${spring.app.jwtExpirationMs}")
    private long jwtExpirationMs;

    /** Single source of truth for refresh lifetime: JWT expiry, cookie max-age and Redis TTL. */
    @Getter
    @Value("${spring.app.jwtRefreshExpirationMs}")
    private long jwtRefreshExpirationMs;

    @Value("${spring.app.emailVerificationTokenExpirationMs}")
    private int emailVerificationTokenExpirationMs;

    @Value("${spring.app.jwtCookieName}")
    private String jwtCookie;

    @Value("${spring.app.refreshTokenCookieName}")
    private String refreshTokenCookie;

    public String generateToken(UserDetailsImpl userDetails) {
        return Jwts.builder()
                .id(UUID.randomUUID().toString()) // Generate a unique ID for the token
                .subject(userDetails.getEmail())
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .claim("roles", userDetails.getAuthorities())
                .claim(CLAIM_TYPE, TYPE_ACCESS)
                .signWith(key())
                .compact();
    }

    public String generateRefreshToken(UserDetailsImpl userDetails) {
        return Jwts.builder()
                .id(UUID.randomUUID().toString()) // Generate a unique ID for the refresh token
                .subject(userDetails.getEmail())
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + jwtRefreshExpirationMs))
                .claim(CLAIM_TYPE, TYPE_REFRESH)
                .signWith(key())
                .compact();
    }

    public String getJwtFromCookies(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, jwtCookie);
        if (cookie != null) {
            try {
                return URLDecoder.decode(cookie.getValue(), StandardCharsets.UTF_8);
            } catch (IllegalArgumentException e) {
                log.warn("Failed to decode JWT cookie value");
                return null;
            }
        }
        return null;
    }

    // Path "/" (not "/api") so the session also reaches /uploads, where private documents
    // are opened directly by the dashboards.
    public ResponseCookie generateJwtCookie(String jwt) {
        return ResponseCookie.from(jwtCookie, jwt)
                .path("/")
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .maxAge(jwtExpirationMs / 1000) // same lifetime as the access token
                .build();
    }

    public ResponseCookie generateRefreshCookie(String refreshToken) {
        return ResponseCookie.from(refreshTokenCookie, refreshToken)
                .path("/api/auth/refresh-token")
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .maxAge(jwtRefreshExpirationMs / 1000) // same lifetime as the refresh token
                .build();
    }

    public ResponseCookie getCleanJwtCookie() {
        return ResponseCookie.from(jwtCookie, null)
                .path("/")
                .build();
    }

    public ResponseCookie getCleanRefreshCookie() {
        return ResponseCookie.from(refreshTokenCookie, null)
                .path("/api/auth/refresh-token")
                .build();
    }

    /**
     * Returns the subject (email) only if the token is correctly signed, not expired
     * and of the expected type; otherwise {@code null}.
     */
    public String getUserNameFromJwtToken(String token, String expectedType) {
        Claims claims = parseClaims(token);
        if (claims == null || !expectedType.equals(claims.get(CLAIM_TYPE, String.class))) {
            return null;
        }
        return claims.getSubject();
    }

    public boolean validateJwtToken(String token, String expectedType) {
        return getUserNameFromJwtToken(token, expectedType) != null;
    }

    private Claims parseClaims(String token) {
        if (!StringUtils.hasText(token)) {
            return null;
        }
        try {
            return Jwts.parser()
                    .verifyWith((SecretKey) key())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.debug("JWT token is expired: {}", e.getMessage());
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
        }
        return null;
    }

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    public String getRefreshTokenFromCookies(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, refreshTokenCookie);
//        System.out.println("Refresh Token Cookie: " + cookie);
        if (cookie != null) {
            try {
                return URLDecoder.decode(cookie.getValue(), StandardCharsets.UTF_8);
            } catch (IllegalArgumentException e) {
                log.warn("Failed to decode refresh token cookie value");
                return null;
            }
        }
        return null;
    }

    public String getJwtFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Remove Bearer prefix
        }
        return null;
    }

    public String getRefreshTokenFromHeader(HttpServletRequest request) {
        String refreshToken = request.getHeader("X-Refresh-Token");
//        System.out.println("Refresh Token: " + refreshToken);
        if (StringUtils.hasText(refreshToken)) {
            return refreshToken;
        }
        return null;
    }

    /** Access token from the cookie or the Authorization header, or {@code null} if absent. */
    public String findJwtToken(HttpServletRequest request) {
        String jwt = getJwtFromCookies(request);
        if (!StringUtils.hasText(jwt)) {
            jwt = getJwtFromHeader(request);
        }
        return StringUtils.hasText(jwt) ? jwt : null;
    }

    public String getJwtToken(HttpServletRequest request) {
        String jwt = findJwtToken(request);
        if (jwt == null) {
            log.warn("JWT token not found in cookies or Authorization header");
            throw new UnauthorizedException("Authentication required: No valid JWT found");
        }

        return jwt;
    }

    public String getRefreshToken(HttpServletRequest request) {
        String refreshToken = getRefreshTokenFromCookies(request);

        // If not found in cookies or empty, try Authorization header
        if (refreshToken == null || refreshToken.isEmpty()) {
            refreshToken = getRefreshTokenFromHeader(request);
        }

        // Final validation
        if (refreshToken == null || refreshToken.isEmpty()) {
            log.warn("Refresh token not found in cookies or Authorization header");
            throw new UnauthorizedException("Authentication required: No valid refresh token found");
        }

        return refreshToken;
    }

    public String generateEmailVerificationJwt(User user) {
        return Jwts.builder()
                .id(UUID.randomUUID().toString()) // Generate a unique ID for the token
                .subject(user.getEmail())
                .claim("userId", user.getUserId())
                .claim(CLAIM_TYPE, TYPE_EMAIL_VERIFICATION)
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + emailVerificationTokenExpirationMs))
                .signWith(key())  // You could have a separate key
                .compact();
    }

}
