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
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JwtUtils {
    @Value("${spring.app.jwtSecret}")
    private String jwtSecret;

    @Value("${spring.app.jwtExpirationMs}")
    private int jwtExpirationMs;

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
                .signWith(key())
                .compact();
    }

    public String generateRefreshToken(UserDetailsImpl userDetails) {
        return Jwts.builder()
                .id(UUID.randomUUID().toString()) // Generate a unique ID for the refresh token
                .subject(userDetails.getEmail())
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + jwtExpirationMs * 24L)) // 24x longer expiry
                .claim("roles", userDetails.getAuthorities())
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

    public ResponseCookie generateJwtCookie(String jwt) {
        return ResponseCookie.from(jwtCookie, jwt)
                .path("/api")
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")// "Lax" is more compatible with modern browsers
                .maxAge(24 * 60 * 60)// 1 day
                .build();
    }

    public ResponseCookie generateRefreshCookie(String refreshToken) {
        return ResponseCookie.from(refreshTokenCookie, refreshToken)
                .path("/api/auth/refresh-token")
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")// "Strict" is more secure for refresh tokens
                .maxAge(7 * 24 * 60 * 60)// 7 days  
                .build();
    }

    public ResponseCookie getCleanJwtCookie() {
        return ResponseCookie.from(jwtCookie, null)
                .path("/api")
                .build();
    }

    public ResponseCookie getCleanRefreshCookie() {
        return ResponseCookie.from(refreshTokenCookie, null)
                .path("/api/auth/refresh-token")
                .build();
    }

    public String getUserNameFromJwtToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith((SecretKey) key())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return claims.getSubject();
        } catch (Exception e) {
            log.error("Cannot extract username from token", e);
            return null;
        }
    }

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    public boolean validateJwtToken(String authToken) {
        try {
            System.out.println("Validate");
            Jwts.parser().verifyWith((SecretKey) key()).build().parseSignedClaims(authToken);
            return true;
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
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

    public String getJwtToken(HttpServletRequest request) {
        String jwt = getJwtFromCookies(request);

        // If not found in cookies or empty, try Authorization header
        if (jwt == null || jwt.isEmpty()) {
            jwt = getJwtFromHeader(request);
        }

        // Final validation
        if (jwt == null || jwt.isEmpty()) {
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
                .claim("type", "EMAIL_VERIFICATION")
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + emailVerificationTokenExpirationMs))
                .signWith(key())  // You could have a separate key
                .compact();
    }

}
