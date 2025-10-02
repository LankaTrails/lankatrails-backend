package com.lankatrails.testdata.builders;

import com.lankatrails.lankatrails_backend.dtos.request.LoginRequest;

/**
 * Fluent builder for LoginRequest test data.
 * Provides convenient factory methods and fluent setters for creating
 * login request DTOs in tests.
 * 
 * Usage Examples:
 * <pre>
 * // Basic login request
 * LoginRequest request = aLoginRequest().build();
 * 
 * // Tourist login
 * LoginRequest touristLogin = aTouristLoginRequest()
 *     .withEmail("tourist@example.com")
 *     .withPassword("password123")
 *     .build();
 * 
 * // Provider login  
 * LoginRequest providerLogin = aProviderLoginRequest()
 *     .withEmail("provider@business.com")
 *     .build();
 * 
 * // Admin login
 * LoginRequest adminLogin = anAdminLoginRequest()
 *     .withEmail("admin@lankatrails.com")
 *     .build();
 * 
 * // Invalid login for testing validation
 * LoginRequest invalidLogin = aLoginRequest()
 *     .withEmptyCredentials()
 *     .build();
 * </pre>
 */
public class LoginRequestBuilder {
    
    private String email;
    private String password;
    
    private LoginRequestBuilder() {
        initializeDefaults();
    }
    
    private void initializeDefaults() {
        this.email = "user@example.com";
        this.password = "Password123";
    }
    
    // =================
    // Factory Methods
    // =================
    
    public static LoginRequestBuilder aLoginRequest() {
        return new LoginRequestBuilder();
    }
    
    public static LoginRequestBuilder aTouristLoginRequest() {
        return new LoginRequestBuilder()
            .withEmail("tourist@example.com")
            .withPassword("TouristPass123");
    }
    
    public static LoginRequestBuilder aProviderLoginRequest() {
        return new LoginRequestBuilder()
            .withEmail("provider@business.com")
            .withPassword("ProviderPass123");
    }
    
    public static LoginRequestBuilder anAdminLoginRequest() {
        return new LoginRequestBuilder()
            .withEmail("admin@lankatrails.com")
            .withPassword("AdminPass123");
    }
    
    public static LoginRequestBuilder aValidLoginRequest() {
        return new LoginRequestBuilder()
            .withEmail("valid.user@example.com")
            .withPassword("ValidPassword123");
    }
    
    public static LoginRequestBuilder anInvalidLoginRequest() {
        return new LoginRequestBuilder()
            .withEmail("invalid-email")
            .withPassword("");
    }
    
    // =================
    // Fluent Setters
    // =================
    
    public LoginRequestBuilder withEmail(String email) {
        this.email = email;
        return this;
    }
    
    public LoginRequestBuilder withPassword(String password) {
        this.password = password;
        return this;
    }
    
    public LoginRequestBuilder withCredentials(String email, String password) {
        this.email = email;
        this.password = password;
        return this;
    }
    
    // =================
    // Convenience Methods
    // =================
    
    public LoginRequestBuilder withValidEmail() {
        this.email = "valid.user@example.com";
        return this;
    }
    
    public LoginRequestBuilder withInvalidEmail() {
        this.email = "invalid-email-format";
        return this;
    }
    
    public LoginRequestBuilder withEmptyEmail() {
        this.email = "";
        return this;
    }
    
    public LoginRequestBuilder withNullEmail() {
        this.email = null;
        return this;
    }
    
    public LoginRequestBuilder withValidPassword() {
        this.password = "ValidPassword123";
        return this;
    }
    
    public LoginRequestBuilder withEmptyPassword() {
        this.password = "";
        return this;
    }
    
    public LoginRequestBuilder withNullPassword() {
        this.password = null;
        return this;
    }
    
    public LoginRequestBuilder withEmptyCredentials() {
        this.email = "";
        this.password = "";
        return this;
    }
    
    public LoginRequestBuilder withNullCredentials() {
        this.email = null;
        this.password = null;
        return this;
    }
    
    public LoginRequestBuilder withBlankSpaces() {
        this.email = "   ";
        this.password = "   ";
        return this;
    }
    
    // Common test user credentials
    public LoginRequestBuilder withExistingTouristCredentials() {
        this.email = "john.doe@example.com";
        this.password = "Password123";
        return this;
    }
    
    public LoginRequestBuilder withExistingProviderCredentials() {
        this.email = "lanka.tours@business.com";
        this.password = "BusinessPass123";
        return this;
    }
    
    public LoginRequestBuilder withExistingAdminCredentials() {
        this.email = "admin@lankatrails.com";
        this.password = "AdminSecure123";
        return this;
    }
    
    public LoginRequestBuilder withNonExistentUserCredentials() {
        this.email = "nonexistent@example.com";
        this.password = "Password123";
        return this;
    }
    
    public LoginRequestBuilder withWrongPasswordCredentials() {
        this.email = "john.doe@example.com";
        this.password = "WrongPassword";
        return this;
    }
    
    // =================
    // Build Method
    // =================
    
    public LoginRequest build() {
        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword(password);
        return request;
    }
}