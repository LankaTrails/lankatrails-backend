package com.lankatrails.testdata.builders;

import java.time.LocalDateTime;

import com.lankatrails.lankatrails_backend.model.Tourist;
import com.lankatrails.lankatrails_backend.model.enums.UserRole;
import com.lankatrails.lankatrails_backend.model.enums.UserStatus;

/**
 * Fluent builder for creating Tourist test entities with sensible defaults.
 * 
 * Thread-safe and immutable - each with() method returns a new builder instance.
 * Extends User functionality with Tourist-specific fields.
 * 
 * Usage:
 * <pre>
 * Tourist tourist = TouristBuilder.aTourist()
 *     .withEmail("tourist@example.com")
 *     .withName("John", "Doe")
 *     .withCountry("USA")
 *     .withVerifiedEmail()
 *     .build();
 * </pre>
 * 
 * @author LankaTrails Development Team
 */
public class TouristBuilder {
    // User fields
    private Long userId;
    private String email;
    private String password;
    private UserRole role;
    private UserStatus status;
    private boolean emailVerified;
    private String profilePictureUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Tourist-specific fields
    private String firstName;
    private String lastName;
    private String country;
    private String phoneNumber;

    private TouristBuilder() {
        // Private constructor - use static factory methods
        this.email = "tourist@example.com";
        this.password = "encodedPassword123";
        this.role = UserRole.ROLE_TOURIST;
        this.status = UserStatus.ACTIVE;
        this.emailVerified = true;
        this.createdAt = LocalDateTime.now().minusDays(1);
        this.updatedAt = LocalDateTime.now();
        
        // Tourist defaults
        this.firstName = "John";
        this.lastName = "Doe";
        this.country = "Sri Lanka";
        this.phoneNumber = "+94771234567";
    }

    private TouristBuilder(TouristBuilder other) {
        // Copy constructor for immutability
        this.userId = other.userId;
        this.email = other.email;
        this.password = other.password;
        this.role = other.role;
        this.status = other.status;
        this.emailVerified = other.emailVerified;
        this.profilePictureUrl = other.profilePictureUrl;
        this.createdAt = other.createdAt;
        this.updatedAt = other.updatedAt;
        
        this.firstName = other.firstName;
        this.lastName = other.lastName;
        this.country = other.country;
        this.phoneNumber = other.phoneNumber;
    }

    // =====================================================
    // STATIC FACTORY METHODS
    // =====================================================

    /**
     * Creates a new TouristBuilder with default values
     */
    public static TouristBuilder aTourist() {
        return new TouristBuilder();
    }

    /**
     * Creates a new TouristBuilder for an international tourist
     */
    public static TouristBuilder anInternationalTourist() {
        return new TouristBuilder()
                .withEmail("international@example.com")
                .withName("James", "Smith")
                .withCountry("United States")
                .withPhoneNumber("+1234567890");
    }

    /**
     * Creates a new TouristBuilder for a local Sri Lankan tourist
     */
    public static TouristBuilder aLocalTourist() {
        return new TouristBuilder()
                .withEmail("local@example.lk")
                .withName("Kasun", "Perera")
                .withCountry("Sri Lanka")
                .withPhoneNumber("+94771234567");
    }

    // =====================================================
    // USER FLUENT BUILDER METHODS
    // =====================================================

    public TouristBuilder withId(Long userId) {
        TouristBuilder copy = new TouristBuilder(this);
        copy.userId = userId;
        return copy;
    }

    public TouristBuilder withEmail(String email) {
        TouristBuilder copy = new TouristBuilder(this);
        copy.email = email;
        return copy;
    }

    public TouristBuilder withPassword(String password) {
        TouristBuilder copy = new TouristBuilder(this);
        copy.password = password;
        return copy;
    }

    public TouristBuilder withRole(UserRole role) {
        TouristBuilder copy = new TouristBuilder(this);
        copy.role = role;
        return copy;
    }

    public TouristBuilder withStatus(UserStatus status) {
        TouristBuilder copy = new TouristBuilder(this);
        copy.status = status;
        return copy;
    }

    public TouristBuilder withEmailVerified(boolean emailVerified) {
        TouristBuilder copy = new TouristBuilder(this);
        copy.emailVerified = emailVerified;
        return copy;
    }

    public TouristBuilder withProfilePictureUrl(String profilePictureUrl) {
        TouristBuilder copy = new TouristBuilder(this);
        copy.profilePictureUrl = profilePictureUrl;
        return copy;
    }

    public TouristBuilder withCreatedAt(LocalDateTime createdAt) {
        TouristBuilder copy = new TouristBuilder(this);
        copy.createdAt = createdAt;
        return copy;
    }

    public TouristBuilder withUpdatedAt(LocalDateTime updatedAt) {
        TouristBuilder copy = new TouristBuilder(this);
        copy.updatedAt = updatedAt;
        return copy;
    }

    // =====================================================
    // TOURIST-SPECIFIC FLUENT BUILDER METHODS
    // =====================================================

    public TouristBuilder withFirstName(String firstName) {
        TouristBuilder copy = new TouristBuilder(this);
        copy.firstName = firstName;
        return copy;
    }

    public TouristBuilder withLastName(String lastName) {
        TouristBuilder copy = new TouristBuilder(this);
        copy.lastName = lastName;
        return copy;
    }

    public TouristBuilder withName(String firstName, String lastName) {
        TouristBuilder copy = new TouristBuilder(this);
        copy.firstName = firstName;
        copy.lastName = lastName;
        return copy;
    }

    public TouristBuilder withCountry(String country) {
        TouristBuilder copy = new TouristBuilder(this);
        copy.country = country;
        return copy;
    }

    public TouristBuilder withPhoneNumber(String phoneNumber) {
        TouristBuilder copy = new TouristBuilder(this);
        copy.phoneNumber = phoneNumber;
        return copy;
    }

    // =====================================================
    // CONVENIENCE METHODS
    // =====================================================

    /**
     * Sets email as verified (emailVerified = true)
     */
    public TouristBuilder withVerifiedEmail() {
        return withEmailVerified(true);
    }

    /**
     * Sets email as unverified (emailVerified = false)
     */
    public TouristBuilder withUnverifiedEmail() {
        return withEmailVerified(false);
    }

    /**
     * Sets user status as PENDING
     */
    public TouristBuilder withPendingStatus() {
        return withStatus(UserStatus.PENDING);
    }

    /**
     * Sets user status as ACTIVE
     */
    public TouristBuilder withActiveStatus() {
        return withStatus(UserStatus.ACTIVE);
    }

    /**
     * Sets user status as DISABLED
     */
    public TouristBuilder withDisabledStatus() {
        return withStatus(UserStatus.DISABLED);
    }

    /**
     * Sets a realistic encoded password for testing
     */
    public TouristBuilder withEncodedPassword() {
        return withPassword("$2a$10$N9qo8uLOickgx2ZMRZoMpOF5i1k1HhGSNcxRJJZ2pMJtQV1aZzs2u"); // "password123"
    }

    /**
     * Sets a plain text password (for request DTOs, not entities)
     */
    public TouristBuilder withPlainPassword(String plainPassword) {
        return withPassword(plainPassword);
    }

    // =====================================================
    // BUILD METHOD
    // =====================================================

    /**
     * Builds and returns the Tourist entity with the configured values
     */
    public Tourist build() {
        Tourist tourist = new Tourist();
        
        // Set User fields
        tourist.setUserId(userId);
        tourist.setEmail(email);
        tourist.setPassword(password);
        tourist.setRole(role);
        tourist.setStatus(status);
        tourist.setEmailVerified(emailVerified);
        tourist.setProfilePictureUrl(profilePictureUrl);
        tourist.setCreatedAt(createdAt);
        tourist.setUpdatedAt(updatedAt);
        
        // Set Tourist-specific fields
        tourist.setFirstName(firstName);
        tourist.setLastName(lastName);
        tourist.setCountry(country);
        tourist.setPhoneNumber(phoneNumber);
        
        return tourist;
    }
}