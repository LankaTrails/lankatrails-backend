package com.lankatrails.testdata.builders;

import java.time.LocalDateTime;

import com.lankatrails.lankatrails_backend.model.User;
import com.lankatrails.lankatrails_backend.model.enums.UserRole;
import com.lankatrails.lankatrails_backend.model.enums.UserStatus;

/**
 * Fluent builder for creating User test entities with sensible defaults.
 * 
 * Thread-safe and immutable - each with() method returns a new builder instance.
 * 
 * Usage:
 * <pre>
 * User user = UserBuilder.aUser()
 *     .withEmail("test@example.com")
 *     .withRole(UserRole.ROLE_TOURIST)
 *     .withVerifiedEmail()
 *     .build();
 * </pre>
 * 
 * @author LankaTrails Development Team
 */
public class UserBuilder {
    private Long userId;
    private String email;
    private String password;
    private UserRole role;
    private UserStatus status;
    private boolean emailVerified;
    private String profilePictureUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private UserBuilder() {
        // Private constructor - use static factory methods
        this.email = "test@example.com";
        this.password = "encodedPassword123";
        this.role = UserRole.ROLE_TOURIST;
        this.status = UserStatus.ACTIVE;
        this.emailVerified = true;
        this.createdAt = LocalDateTime.now().minusDays(1);
        this.updatedAt = LocalDateTime.now();
    }

    public UserBuilder(UserBuilder other) {
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
    }

    // =====================================================
    // STATIC FACTORY METHODS
    // =====================================================

    /**
     * Creates a new UserBuilder with default values for a typical user
     */
    public static UserBuilder aUser() {
        return new UserBuilder();
    }

    /**
     * Creates a new UserBuilder configured as a tourist user
     */
    public static UserBuilder aTouristUser() {
        return new UserBuilder()
                .withRole(UserRole.ROLE_TOURIST)
                .withEmail("tourist@example.com");
    }

    /**
     * Creates a new UserBuilder configured as a provider user
     */
    public static UserBuilder aProviderUser() {
        return new UserBuilder()
                .withRole(UserRole.ROLE_PROVIDER)
                .withEmail("provider@example.com")
                .withStatus(UserStatus.PENDING);
    }

    /**
     * Creates a new UserBuilder configured as an admin user
     */
    public static UserBuilder anAdminUser() {
        return new UserBuilder()
                .withRole(UserRole.ROLE_ADMIN)
                .withEmail("admin@example.com");
    }

    // =====================================================
    // FLUENT BUILDER METHODS
    // =====================================================

    public UserBuilder withId(Long userId) {
        UserBuilder copy = new UserBuilder(this);
        copy.userId = userId;
        return copy;
    }

    public UserBuilder withEmail(String email) {
        UserBuilder copy = new UserBuilder(this);
        copy.email = email;
        return copy;
    }

    public UserBuilder withPassword(String password) {
        UserBuilder copy = new UserBuilder(this);
        copy.password = password;
        return copy;
    }

    public UserBuilder withRole(UserRole role) {
        UserBuilder copy = new UserBuilder(this);
        copy.role = role;
        return copy;
    }

    public UserBuilder withStatus(UserStatus status) {
        UserBuilder copy = new UserBuilder(this);
        copy.status = status;
        return copy;
    }

    public UserBuilder withEmailVerified(boolean emailVerified) {
        UserBuilder copy = new UserBuilder(this);
        copy.emailVerified = emailVerified;
        return copy;
    }

    public UserBuilder withProfilePictureUrl(String profilePictureUrl) {
        UserBuilder copy = new UserBuilder(this);
        copy.profilePictureUrl = profilePictureUrl;
        return copy;
    }

    public UserBuilder withCreatedAt(LocalDateTime createdAt) {
        UserBuilder copy = new UserBuilder(this);
        copy.createdAt = createdAt;
        return copy;
    }

    public UserBuilder withUpdatedAt(LocalDateTime updatedAt) {
        UserBuilder copy = new UserBuilder(this);
        copy.updatedAt = updatedAt;
        return copy;
    }

    // =====================================================
    // CONVENIENCE METHODS
    // =====================================================

    /**
     * Sets email as verified (emailVerified = true)
     */
    public UserBuilder withVerifiedEmail() {
        return withEmailVerified(true);
    }

    /**
     * Sets email as unverified (emailVerified = false)
     */
    public UserBuilder withUnverifiedEmail() {
        return withEmailVerified(false);
    }

    /**
     * Sets user status as PENDING
     */
    public UserBuilder withPendingStatus() {
        return withStatus(UserStatus.PENDING);
    }

    /**
     * Sets user status as ACTIVE
     */
    public UserBuilder withActiveStatus() {
        return withStatus(UserStatus.ACTIVE);
    }

    /**
     * Sets user status as DISABLED
     */
    public UserBuilder withDisabledStatus() {
        return withStatus(UserStatus.DISABLED);
    }

    /**
     * Sets a realistic encoded password for testing
     */
    public UserBuilder withEncodedPassword() {
        return withPassword("$2a$10$N9qo8uLOickgx2ZMRZoMpOF5i1k1HhGSNcxRJJZ2pMJtQV1aZzs2u"); // "password123"
    }

    /**
     * Sets a plain text password (for request DTOs, not entities)
     */
    public UserBuilder withPlainPassword(String plainPassword) {
        return withPassword(plainPassword);
    }

    // =====================================================
    // BUILD METHOD
    // =====================================================

    /**
     * Builds and returns the User entity with the configured values
     */
    public User build() {
        User user = new User();
        user.setUserId(userId);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole(role);
        user.setStatus(status);
        user.setEmailVerified(emailVerified);
        user.setProfilePictureUrl(profilePictureUrl);
        user.setCreatedAt(createdAt);
        user.setUpdatedAt(updatedAt);
        return user;
    }
}