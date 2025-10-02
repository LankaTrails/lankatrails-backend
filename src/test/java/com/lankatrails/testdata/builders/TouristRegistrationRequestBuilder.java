package com.lankatrails.testdata.builders;

import com.lankatrails.lankatrails_backend.dtos.request.TouristRegistrationRequest;
import com.lankatrails.lankatrails_backend.model.enums.UserRole;

/**
 * Fluent builder for TouristRegistrationRequest test data.
 * Provides convenient factory methods and fluent setters for creating
 * tourist registration request DTOs in tests.
 * 
 * Usage Examples:
 * <pre>
 * // Basic tourist registration
 * TouristRegistrationRequest request = aTouristRegistrationRequest().build();
 * 
 * // International tourist
 * TouristRegistrationRequest international = anInternationalTouristRequest()
 *     .withName("John", "Smith")
 *     .withCountry("United States")
 *     .withEmail("john.smith@example.com")
 *     .build();
 * 
 * // Local tourist
 * TouristRegistrationRequest local = aLocalTouristRequest()
 *     .withName("Kasun", "Perera")
 *     .withPhoneNumber("+94771234567")
 *     .build();
 * 
 * // Validation test data
 * TouristRegistrationRequest invalid = aTouristRegistrationRequest()
 *     .withInvalidEmail("invalid-email")
 *     .withWeakPassword("123")
 *     .build();
 * </pre>
 */
public class TouristRegistrationRequestBuilder {
    
    private String email;
    private String password;
    private UserRole userRole;
    private String profilePictureUrl;
    private String firstName;
    private String lastName;
    private String country;
    private String phoneNumber;
    
    private TouristRegistrationRequestBuilder() {
        initializeDefaults();
    }
    
    private void initializeDefaults() {
        this.email = "tourist@example.com";
        this.password = "Password123";
        this.userRole = UserRole.ROLE_TOURIST;
        this.firstName = "Test";
        this.lastName = "Tourist";
        this.country = "Sri Lanka";
        this.phoneNumber = "+94771234567";
    }
    
    // =================
    // Factory Methods
    // =================
    
    public static TouristRegistrationRequestBuilder aTouristRegistrationRequest() {
        return new TouristRegistrationRequestBuilder();
    }
    
    public static TouristRegistrationRequestBuilder anInternationalTouristRequest() {
        return new TouristRegistrationRequestBuilder()
            .withName("John", "Smith")
            .withCountry("United States")
            .withEmail("john.smith@example.com")
            .withPhoneNumber("+1234567890");
    }
    
    public static TouristRegistrationRequestBuilder aLocalTouristRequest() {
        return new TouristRegistrationRequestBuilder()
            .withName("Kasun", "Perera")
            .withCountry("Sri Lanka")
            .withEmail("kasun.perera@gmail.com")
            .withPhoneNumber("+94771234567");
    }
    
    public static TouristRegistrationRequestBuilder aValidTouristRequest() {
        return new TouristRegistrationRequestBuilder()
            .withValidEmail()
            .withStrongPassword()
            .withValidName();
    }
    
    public static TouristRegistrationRequestBuilder anInvalidTouristRequest() {
        return new TouristRegistrationRequestBuilder()
            .withInvalidEmail("invalid-email")
            .withWeakPassword("123");
    }
    
    // =================
    // Fluent Setters
    // =================
    
    public TouristRegistrationRequestBuilder withEmail(String email) {
        this.email = email;
        return this;
    }
    
    public TouristRegistrationRequestBuilder withPassword(String password) {
        this.password = password;
        return this;
    }
    
    public TouristRegistrationRequestBuilder withUserRole(UserRole userRole) {
        this.userRole = userRole;
        return this;
    }
    
    public TouristRegistrationRequestBuilder withProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
        return this;
    }
    
    public TouristRegistrationRequestBuilder withFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }
    
    public TouristRegistrationRequestBuilder withLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }
    
    public TouristRegistrationRequestBuilder withName(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
        return this;
    }
    
    public TouristRegistrationRequestBuilder withCountry(String country) {
        this.country = country;
        return this;
    }
    
    public TouristRegistrationRequestBuilder withPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }
    
    // =================
    // Convenience Methods
    // =================
    
    public TouristRegistrationRequestBuilder withValidEmail() {
        this.email = "valid.tourist@example.com";
        return this;
    }
    
    public TouristRegistrationRequestBuilder withInvalidEmail(String invalidEmail) {
        this.email = invalidEmail;
        return this;
    }
    
    public TouristRegistrationRequestBuilder withStrongPassword() {
        this.password = "StrongPassword123";
        return this;
    }
    
    public TouristRegistrationRequestBuilder withWeakPassword(String weakPassword) {
        this.password = weakPassword;
        return this;
    }
    
    public TouristRegistrationRequestBuilder withValidName() {
        this.firstName = "Valid";
        this.lastName = "Tourist";
        return this;
    }
    
    public TouristRegistrationRequestBuilder withLongFirstName() {
        this.firstName = "VeryLongFirstNameThatExceedsTheMaximumAllowedLength";
        return this;
    }
    
    public TouristRegistrationRequestBuilder withLongLastName() {
        this.lastName = "VeryLongLastNameThatExceedsTheMaximumAllowedLength";
        return this;
    }
    
    public TouristRegistrationRequestBuilder withLongCountry() {
        this.country = "VeryLongCountryNameThatExceedsTheMaximumAllowedLength";
        return this;
    }
    
    public TouristRegistrationRequestBuilder withLongPhoneNumber() {
        this.phoneNumber = "VeryLongPhoneNumberThatExceedsTheMaximumAllowedLength";
        return this;
    }
    
    public TouristRegistrationRequestBuilder withEmptyFields() {
        this.email = "";
        this.password = "";
        this.firstName = "";
        this.lastName = "";
        this.country = "";
        this.phoneNumber = "";
        return this;
    }
    
    public TouristRegistrationRequestBuilder withNullFields() {
        this.email = null;
        this.password = null;
        this.firstName = null;
        this.lastName = null;
        this.country = null;
        this.phoneNumber = null;
        return this;
    }
    
    // =================
    // Build Method
    // =================
    
    public TouristRegistrationRequest build() {
        TouristRegistrationRequest request = new TouristRegistrationRequest();
        request.setEmail(email);
        request.setPassword(password);
        request.setUserRole(userRole);
        request.setProfilePictureUrl(profilePictureUrl);
        request.setFirstName(firstName);
        request.setLastName(lastName);
        request.setCountry(country);
        request.setPhoneNumber(phoneNumber);
        return request;
    }
}