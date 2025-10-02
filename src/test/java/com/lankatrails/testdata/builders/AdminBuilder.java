package com.lankatrails.testdata.builders;

import java.time.LocalDateTime;

import com.lankatrails.lankatrails_backend.model.Admin;
import com.lankatrails.lankatrails_backend.model.enums.UserRole;
import com.lankatrails.lankatrails_backend.model.enums.UserStatus;

/**
 * Fluent builder for Admin entity test data.
 * Provides a comprehensive set of factory methods and fluent setters
 * for creating Admin instances in tests.
 * 
 * Usage Examples:
 * <pre>
 * // Basic admin
 * Admin admin = anAdmin().build();
 * 
 * // Admin with full name
 * Admin namedAdmin = anAdmin()
 *     .withName("John", "Doe")
 *     .withVerifiedEmail()
 *     .build();
 * 
 * // System admin
 * Admin systemAdmin = aSystemAdmin()
 *     .withEmail("admin@lankatrails.com")
 *     .withActiveStatus()
 *     .build();
 * 
 * // Senior admin with custom details
 * Admin seniorAdmin = aSeniorAdmin()
 *     .withName("Jane", "Smith")
 *     .withEmail("jane.smith@lankatrails.com")
 *     .withVerifiedEmail()
 *     .build();
 * </pre>
 */
public class AdminBuilder {
    
    // User inherited fields (delegated to UserBuilder)
    private UserBuilder userBuilder;
    
    // Admin-specific fields
    private String firstName;
    private String lastName;
    
    private AdminBuilder() {
        this.userBuilder = UserBuilder.aUser()
            .withRole(UserRole.ROLE_ADMIN)
            .withStatus(UserStatus.ACTIVE)
            .withVerifiedEmail(); // Admins typically have verified emails
        initializeDefaults();
    }
    
    private AdminBuilder(AdminBuilder other) {
        this.userBuilder = new UserBuilder(other.userBuilder);
        this.firstName = other.firstName;
        this.lastName = other.lastName;
    }
    
    private void initializeDefaults() {
        this.firstName = "Admin";
        this.lastName = "User";
    }
    
    // =================
    // Factory Methods
    // =================
    
    public static AdminBuilder anAdmin() {
        return new AdminBuilder();
    }
    
    public static AdminBuilder aSystemAdmin() {
        return new AdminBuilder()
            .withName("System", "Administrator")
            .withEmail("admin@lankatrails.com");
    }
    
    public static AdminBuilder aSeniorAdmin() {
        return new AdminBuilder()
            .withName("Senior", "Admin")
            .withEmail("senior.admin@lankatrails.com");
    }
    
    public static AdminBuilder aSuperAdmin() {
        return new AdminBuilder()
            .withName("Super", "Admin")
            .withEmail("super.admin@lankatrails.com");
    }
    
    public static AdminBuilder aSupportAdmin() {
        return new AdminBuilder()
            .withName("Support", "Admin")
            .withEmail("support@lankatrails.com");
    }
    
    public static AdminBuilder aContentAdmin() {
        return new AdminBuilder()
            .withName("Content", "Manager")
            .withEmail("content.admin@lankatrails.com");
    }
    
    // =================
    // User Delegation Methods
    // =================
    
    public AdminBuilder withUserId(Long userId) {
        this.userBuilder.withId(userId);
        return this;
    }
    
    public AdminBuilder withEmail(String email) {
        this.userBuilder.withEmail(email);
        return this;
    }
    
    public AdminBuilder withPassword(String password) {
        this.userBuilder.withPassword(password);
        return this;
    }
    
    public AdminBuilder withStatus(UserStatus status) {
        this.userBuilder.withStatus(status);
        return this;
    }
    
    public AdminBuilder withRole(UserRole role) {
        this.userBuilder.withRole(role);
        return this;
    }
    
    public AdminBuilder withEmailVerified(boolean emailVerified) {
        this.userBuilder.withEmailVerified(emailVerified);
        return this;
    }
    
    public AdminBuilder withVerifiedEmail() {
        this.userBuilder.withVerifiedEmail();
        return this;
    }
    
    public AdminBuilder withUnverifiedEmail() {
        this.userBuilder.withUnverifiedEmail();
        return this;
    }
    
    public AdminBuilder withCreatedAt(LocalDateTime createdAt) {
        this.userBuilder.withCreatedAt(createdAt);
        return this;
    }
    
    public AdminBuilder withUpdatedAt(LocalDateTime updatedAt) {
        this.userBuilder.withUpdatedAt(updatedAt);
        return this;
    }
    
    public AdminBuilder withActiveStatus() {
        this.userBuilder.withActiveStatus();
        return this;
    }
    
    public AdminBuilder withPendingStatus() {
        this.userBuilder.withPendingStatus();
        return this;
    }
    
    public AdminBuilder withDisabledStatus() {
        this.userBuilder.withDisabledStatus();
        return this;
    }
    
    public AdminBuilder withEncodedPassword() {
        this.userBuilder.withEncodedPassword();
        return this;
    }
    
    public AdminBuilder withPlainPassword(String plainPassword) {
        this.userBuilder.withPlainPassword(plainPassword);
        return this;
    }
    
    // =================
    // Admin-Specific Methods
    // =================
    
    public AdminBuilder withFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }
    
    public AdminBuilder withLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }
    
    public AdminBuilder withName(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
        return this;
    }
    
    public AdminBuilder withFullName(String fullName) {
        String[] parts = fullName.split(" ", 2);
        this.firstName = parts[0];
        this.lastName = parts.length > 1 ? parts[1] : "";
        return this;
    }
    
    // =================
    // Convenience Methods
    // =================
    
    public AdminBuilder withRandomName() {
        String[] firstNames = {"John", "Jane", "Michael", "Sarah", "David", "Lisa", "Robert", "Mary"};
        String[] lastNames = {"Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis"};
        
        int firstIndex = (int) (Math.random() * firstNames.length);
        int lastIndex = (int) (Math.random() * lastNames.length);
        
        this.firstName = firstNames[firstIndex];
        this.lastName = lastNames[lastIndex];
        return this;
    }
    
    public AdminBuilder withAdminEmail() {
        String email = (firstName + "." + lastName + "@lankatrails.com").toLowerCase();
        this.userBuilder.withEmail(email);
        return this;
    }
    
    public AdminBuilder withAdminEmailAndName(String firstName, String lastName) {
        return withName(firstName, lastName)
                .withAdminEmail();
    }
    
    // =================
    // Build Method
    // =================
    
    public Admin build() {
        Admin admin = new Admin();
        
        // Set User fields through UserBuilder
        var user = userBuilder.build();
        admin.setUserId(user.getUserId());
        admin.setEmail(user.getEmail());
        admin.setPassword(user.getPassword());
        admin.setRole(user.getRole());
        admin.setStatus(user.getStatus());
        admin.setEmailVerified(user.getEmailVerified());
        admin.setCreatedAt(user.getCreatedAt());
        admin.setUpdatedAt(user.getUpdatedAt());
        
        // Set Admin-specific fields
        admin.setFirstName(firstName);
        admin.setLastName(lastName);
        
        return admin;
    }
}