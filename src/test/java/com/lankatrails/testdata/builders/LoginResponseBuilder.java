package com.lankatrails.testdata.builders;

import com.lankatrails.lankatrails_backend.dtos.response.LoginResponse;
import com.lankatrails.lankatrails_backend.model.enums.UserRole;

/**
 * Fluent builder for LoginResponse test data.
 * Provides convenient factory methods and fluent setters for creating
 * login response DTOs in tests.
 * 
 * Usage Examples:
 * <pre>
 * // Basic login response
 * LoginResponse response = aLoginResponse().build();
 * 
 * // Tourist login response
 * LoginResponse touristResponse = aTouristLoginResponse()
 *     .withEmail("tourist@example.com")
 *     .withVerifiedEmail()
 *     .build();
 * 
 * // Provider login response
 * LoginResponse providerResponse = aProviderLoginResponse()
 *     .withEmail("provider@business.com")
 *     .withRefreshToken("refresh-token-123")
 *     .build();
 * 
 * // Admin login response
 * LoginResponse adminResponse = anAdminLoginResponse()
 *     .withId(1L)
 *     .withEmail("admin@lankatrails.com")
 *     .build();
 * 
 * // Login response with custom JWT
 * LoginResponse customResponse = aLoginResponse()
 *     .withJwtToken("custom.jwt.token")
 *     .withRefreshToken("custom.refresh.token")
 *     .build();
 * </pre>
 */
public class LoginResponseBuilder {
    
    private Long id;
    private String jwtToken;
    private String email;
    private UserRole role;
    private String refreshToken;
    private boolean emailVerified;
    
    private LoginResponseBuilder() {
        initializeDefaults();
    }
    
    private void initializeDefaults() {
        this.id = 1L;
        this.jwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token";
        this.email = "user@example.com";
        this.role = UserRole.ROLE_TOURIST;
        this.refreshToken = "refresh-token-123";
        this.emailVerified = true;
    }
    
    // =================
    // Factory Methods
    // =================
    
    public static LoginResponseBuilder aLoginResponse() {
        return new LoginResponseBuilder();
    }
    
    public static LoginResponseBuilder aTouristLoginResponse() {
        return new LoginResponseBuilder()
            .withRole(UserRole.ROLE_TOURIST)
            .withEmail("tourist@example.com")
            .withId(100L);
    }
    
    public static LoginResponseBuilder aProviderLoginResponse() {
        return new LoginResponseBuilder()
            .withRole(UserRole.ROLE_PROVIDER)
            .withEmail("provider@business.com")
            .withId(200L);
    }
    
    public static LoginResponseBuilder anAdminLoginResponse() {
        return new LoginResponseBuilder()
            .withRole(UserRole.ROLE_ADMIN)
            .withEmail("admin@lankatrails.com")
            .withId(300L);
    }
    
    public static LoginResponseBuilder aValidLoginResponse() {
        return new LoginResponseBuilder()
            .withValidJwtToken()
            .withValidRefreshToken()
            .withVerifiedEmail();
    }
    
    public static LoginResponseBuilder anUnverifiedLoginResponse() {
        return new LoginResponseBuilder()
            .withUnverifiedEmail();
    }
    
    // =================
    // Fluent Setters
    // =================
    
    public LoginResponseBuilder withId(Long id) {
        this.id = id;
        return this;
    }
    
    public LoginResponseBuilder withJwtToken(String jwtToken) {
        this.jwtToken = jwtToken;
        return this;
    }
    
    public LoginResponseBuilder withEmail(String email) {
        this.email = email;
        return this;
    }
    
    public LoginResponseBuilder withRole(UserRole role) {
        this.role = role;
        return this;
    }
    
    public LoginResponseBuilder withRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
        return this;
    }
    
    public LoginResponseBuilder withEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
        return this;
    }
    
    public LoginResponseBuilder withCredentials(String email, UserRole role) {
        this.email = email;
        this.role = role;
        return this;
    }
    
    public LoginResponseBuilder withTokens(String jwtToken, String refreshToken) {
        this.jwtToken = jwtToken;
        this.refreshToken = refreshToken;
        return this;
    }
    
    // =================
    // Convenience Methods
    // =================
    
    public LoginResponseBuilder withVerifiedEmail() {
        this.emailVerified = true;
        return this;
    }
    
    public LoginResponseBuilder withUnverifiedEmail() {
        this.emailVerified = false;
        return this;
    }
    
    public LoginResponseBuilder withValidJwtToken() {
        this.jwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwiaWF0IjoxNjAwMDAwMDAwLCJleHAiOjE2MDAwMDM2MDB9.valid-signature";
        return this;
    }
    
    public LoginResponseBuilder withExpiredJwtToken() {
        this.jwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwiaWF0IjoxNTAwMDAwMDAwLCJleHAiOjE1MDAwMDM2MDB9.expired-signature";
        return this;
    }
    
    public LoginResponseBuilder withInvalidJwtToken() {
        this.jwtToken = "invalid.jwt.token";
        return this;
    }
    
    public LoginResponseBuilder withValidRefreshToken() {
        this.refreshToken = "valid-refresh-token-" + System.currentTimeMillis();
        return this;
    }
    
    public LoginResponseBuilder withExpiredRefreshToken() {
        this.refreshToken = "expired-refresh-token-12345";
        return this;
    }
    
    public LoginResponseBuilder withoutRefreshToken() {
        this.refreshToken = null;
        return this;
    }
    
    public LoginResponseBuilder withEmptyTokens() {
        this.jwtToken = "";
        this.refreshToken = "";
        return this;
    }
    
    public LoginResponseBuilder withNullTokens() {
        this.jwtToken = null;
        this.refreshToken = null;
        return this;
    }
    
    // Role-specific convenience methods
    public LoginResponseBuilder asTourist() {
        this.role = UserRole.ROLE_TOURIST;
        return this;
    }
    
    public LoginResponseBuilder asProvider() {
        this.role = UserRole.ROLE_PROVIDER;
        return this;
    }
    
    public LoginResponseBuilder asAdmin() {
        this.role = UserRole.ROLE_ADMIN;
        return this;
    }
    
    // Common test scenarios
    public LoginResponseBuilder withNewUserScenario() {
        return withId(999L)
                .withUnverifiedEmail()
                .withValidJwtToken()
                .withValidRefreshToken();
    }
    
    public LoginResponseBuilder withExistingUserScenario() {
        return withId(123L)
                .withVerifiedEmail()
                .withValidJwtToken()
                .withValidRefreshToken();
    }
    
    public LoginResponseBuilder withFirstTimeLoginScenario() {
        return withUnverifiedEmail()
                .withValidJwtToken()
                .withoutRefreshToken();
    }
    
    public LoginResponseBuilder withRegularLoginScenario() {
        return withVerifiedEmail()
                .withValidJwtToken()
                .withValidRefreshToken();
    }
    
    // =================
    // Build Method
    // =================
    
    public LoginResponse build() {
        return LoginResponse.builder()
            .id(id)
            .jwtToken(jwtToken)
            .email(email)
            .role(role)
            .refreshToken(refreshToken)
            .emailVerified(emailVerified)
            .build();
    }
}