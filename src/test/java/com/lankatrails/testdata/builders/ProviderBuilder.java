package com.lankatrails.testdata.builders;

import com.lankatrails.lankatrails_backend.model.Provider;
import com.lankatrails.lankatrails_backend.model.ContactPerson;
import com.lankatrails.lankatrails_backend.model.enums.BusinessType;
import com.lankatrails.lankatrails_backend.model.enums.ApprovalStatus;
import com.lankatrails.lankatrails_backend.model.enums.UserRole;
import com.lankatrails.lankatrails_backend.model.enums.UserStatus;

import java.time.LocalDateTime;

/**
 * Fluent builder for Provider entity test data.
 * Provides a comprehensive set of factory methods and fluent setters
 * for creating Provider instances in tests.
 * 
 * Usage Examples:
 * <pre>
 * // Basic provider
 * Provider provider = aProvider().build();
 * 
 * // Accommodation provider with approvals
 * Provider accommodationProvider = anAccommodationProvider()
 *     .withVerifiedEmail()
 *     .withApprovedAccommodation()
 *     .build();
 * 
 * // Tour guide with contact person
 * Provider tourGuide = aTourGuideProvider()
 *     .withContactPerson("John Smith", "john@example.com", "Manager")
 *     .withPendingApproval()
 *     .build();
 * 
 * // Company provider with business registration
 * Provider company = aCompanyProvider()
 *     .withBusinessName("Lanka Adventures Ltd")
 *     .withBusinessRegistration("PV12345", "https://example.com/br.pdf")
 *     .withAllServicesApproved()
 *     .build();
 * </pre>
 */
public class ProviderBuilder {
    
    // User inherited fields (delegated to UserBuilder)
    private UserBuilder userBuilder;
    
    // Provider-specific fields
    private String businessName;
    private String businessDescription;
    private BusinessType businessType;
    private String coverImageUrl;
    private String businessRegistrationNumber;
    private String businessRegistrationUrl;
    private String stripeAccountId;
    private ApprovalStatus accommodationApprovalStatus;
    private ApprovalStatus tourGuideApprovalStatus;
    private ApprovalStatus transportApprovalStatus;
    private ApprovalStatus activityApprovalStatus;
    private ApprovalStatus foodApprovalStatus;
    private ContactPerson contactPerson;
    
    private ProviderBuilder() {
        this.userBuilder = UserBuilder.aUser()
            .withRole(UserRole.ROLE_PROVIDER)
            .withStatus(UserStatus.PENDING);
        initializeDefaults();
    }
    
    private ProviderBuilder(ProviderBuilder other) {
        this.userBuilder = new UserBuilder(other.userBuilder);
        this.businessName = other.businessName;
        this.businessDescription = other.businessDescription;
        this.businessType = other.businessType;
        this.coverImageUrl = other.coverImageUrl;
        this.businessRegistrationNumber = other.businessRegistrationNumber;
        this.businessRegistrationUrl = other.businessRegistrationUrl;
        this.stripeAccountId = other.stripeAccountId;
        this.accommodationApprovalStatus = other.accommodationApprovalStatus;
        this.tourGuideApprovalStatus = other.tourGuideApprovalStatus;
        this.transportApprovalStatus = other.transportApprovalStatus;
        this.activityApprovalStatus = other.activityApprovalStatus;
        this.foodApprovalStatus = other.foodApprovalStatus;
        this.contactPerson = other.contactPerson;
    }
    
    private void initializeDefaults() {
        this.businessName = "Test Business";
        this.businessDescription = "A test business providing quality services";
        this.businessType = BusinessType.INDIVIDUAL;
        this.accommodationApprovalStatus = ApprovalStatus.NOT_REQUESTED;
        this.tourGuideApprovalStatus = ApprovalStatus.NOT_REQUESTED;
        this.transportApprovalStatus = ApprovalStatus.NOT_REQUESTED;
        this.activityApprovalStatus = ApprovalStatus.NOT_REQUESTED;
        this.foodApprovalStatus = ApprovalStatus.NOT_REQUESTED;
    }
    
    // =================
    // Factory Methods
    // =================
    
    public static ProviderBuilder aProvider() {
        return new ProviderBuilder();
    }
    
    public static ProviderBuilder anAccommodationProvider() {
        return new ProviderBuilder()
            .withBusinessName("Sunny Beach Resort")
            .withBusinessDescription("Luxury beachfront accommodation with stunning ocean views")
            .withAccommodationApprovalStatus(ApprovalStatus.APPROVED);
    }
    
    public static ProviderBuilder aTourGuideProvider() {
        return new ProviderBuilder()
            .withBusinessName("Lanka Cultural Tours")
            .withBusinessDescription("Expert cultural and heritage tour guidance")
            .withTourGuideApprovalStatus(ApprovalStatus.APPROVED);
    }
    
    public static ProviderBuilder aTransportProvider() {
        return new ProviderBuilder()
            .withBusinessName("Island Express Transport")
            .withBusinessDescription("Safe and reliable transportation services")
            .withTransportApprovalStatus(ApprovalStatus.APPROVED);
    }
    
    public static ProviderBuilder anActivityProvider() {
        return new ProviderBuilder()
            .withBusinessName("Adventure Lanka")
            .withBusinessDescription("Thrilling outdoor activities and adventures")
            .withActivityApprovalStatus(ApprovalStatus.APPROVED);
    }
    
    public static ProviderBuilder aFoodProvider() {
        return new ProviderBuilder()
            .withBusinessName("Spice Garden Restaurant")
            .withBusinessDescription("Authentic Sri Lankan cuisine experience")
            .withFoodApprovalStatus(ApprovalStatus.APPROVED);
    }
    
    public static ProviderBuilder aCompanyProvider() {
        return new ProviderBuilder()
            .withBusinessType(BusinessType.COMPANY)
            .withBusinessName("Lanka Tours & Travel Ltd")
            .withBusinessDescription("Full-service travel and tourism company");
    }
    
    public static ProviderBuilder anOrganizationProvider() {
        return new ProviderBuilder()
            .withBusinessType(BusinessType.ORGANIZATION)
            .withBusinessName("Sri Lanka Tourism Foundation")
            .withBusinessDescription("Non-profit organization promoting sustainable tourism");
    }
    
    public static ProviderBuilder aPendingProvider() {
        return new ProviderBuilder()
            .withStatus(UserStatus.PENDING)
            .withAllApprovalStatus(ApprovalStatus.PENDING);
    }
    
    public static ProviderBuilder anApprovedProvider() {
        return new ProviderBuilder()
            .withStatus(UserStatus.ACTIVE)
            .withAllApprovalStatus(ApprovalStatus.APPROVED)
            .withVerifiedEmail();
    }
    
    public static ProviderBuilder aRejectedProvider() {
        return new ProviderBuilder()
            .withStatus(UserStatus.DISABLED)
            .withAllApprovalStatus(ApprovalStatus.REJECTED);
    }
    
    // =================
    // User Delegation Methods
    // =================
    
    public ProviderBuilder withUserId(Long userId) {
        this.userBuilder.withId(userId);
        return this;
    }
    
    public ProviderBuilder withEmail(String email) {
        this.userBuilder.withEmail(email);
        return this;
    }
    
    public ProviderBuilder withPassword(String password) {
        this.userBuilder.withPassword(password);
        return this;
    }
    
    public ProviderBuilder withStatus(UserStatus status) {
        this.userBuilder.withStatus(status);
        return this;
    }
    
    public ProviderBuilder withRole(UserRole role) {
        this.userBuilder.withRole(role);
        return this;
    }
    
    public ProviderBuilder withEmailVerified(boolean emailVerified) {
        this.userBuilder.withEmailVerified(emailVerified);
        return this;
    }
    
    public ProviderBuilder withVerifiedEmail() {
        this.userBuilder.withVerifiedEmail();
        return this;
    }
    
    public ProviderBuilder withUnverifiedEmail() {
        this.userBuilder.withUnverifiedEmail();
        return this;
    }
    
    public ProviderBuilder withCreatedAt(LocalDateTime createdAt) {
        this.userBuilder.withCreatedAt(createdAt);
        return this;
    }
    
    public ProviderBuilder withUpdatedAt(LocalDateTime updatedAt) {
        this.userBuilder.withUpdatedAt(updatedAt);
        return this;
    }
    
    // =================
    // Provider-Specific Methods
    // =================
    
    public ProviderBuilder withBusinessName(String businessName) {
        this.businessName = businessName;
        return this;
    }
    
    public ProviderBuilder withBusinessDescription(String businessDescription) {
        this.businessDescription = businessDescription;
        return this;
    }
    
    public ProviderBuilder withBusinessType(BusinessType businessType) {
        this.businessType = businessType;
        return this;
    }
    
    public ProviderBuilder withCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
        return this;
    }
    
    public ProviderBuilder withBusinessRegistrationNumber(String businessRegistrationNumber) {
        this.businessRegistrationNumber = businessRegistrationNumber;
        return this;
    }
    
    public ProviderBuilder withBusinessRegistrationUrl(String businessRegistrationUrl) {
        this.businessRegistrationUrl = businessRegistrationUrl;
        return this;
    }
    
    public ProviderBuilder withBusinessRegistration(String number, String url) {
        this.businessRegistrationNumber = number;
        this.businessRegistrationUrl = url;
        return this;
    }
    
    public ProviderBuilder withStripeAccountId(String stripeAccountId) {
        this.stripeAccountId = stripeAccountId;
        return this;
    }
    
    // Approval Status Methods
    public ProviderBuilder withAccommodationApprovalStatus(ApprovalStatus status) {
        this.accommodationApprovalStatus = status;
        return this;
    }
    
    public ProviderBuilder withTourGuideApprovalStatus(ApprovalStatus status) {
        this.tourGuideApprovalStatus = status;
        return this;
    }
    
    public ProviderBuilder withTransportApprovalStatus(ApprovalStatus status) {
        this.transportApprovalStatus = status;
        return this;
    }
    
    public ProviderBuilder withActivityApprovalStatus(ApprovalStatus status) {
        this.activityApprovalStatus = status;
        return this;
    }
    
    public ProviderBuilder withFoodApprovalStatus(ApprovalStatus status) {
        this.foodApprovalStatus = status;
        return this;
    }
    
    public ProviderBuilder withAllApprovalStatus(ApprovalStatus status) {
        this.accommodationApprovalStatus = status;
        this.tourGuideApprovalStatus = status;
        this.transportApprovalStatus = status;
        this.activityApprovalStatus = status;
        this.foodApprovalStatus = status;
        return this;
    }
    
    // Convenience approval methods
    public ProviderBuilder withApprovedAccommodation() {
        return withAccommodationApprovalStatus(ApprovalStatus.APPROVED);
    }
    
    public ProviderBuilder withApprovedTourGuide() {
        return withTourGuideApprovalStatus(ApprovalStatus.APPROVED);
    }
    
    public ProviderBuilder withApprovedTransport() {
        return withTransportApprovalStatus(ApprovalStatus.APPROVED);
    }
    
    public ProviderBuilder withApprovedActivity() {
        return withActivityApprovalStatus(ApprovalStatus.APPROVED);
    }
    
    public ProviderBuilder withApprovedFood() {
        return withFoodApprovalStatus(ApprovalStatus.APPROVED);
    }
    
    public ProviderBuilder withAllServicesApproved() {
        return withAllApprovalStatus(ApprovalStatus.APPROVED);
    }
    
    public ProviderBuilder withPendingApproval() {
        return withAllApprovalStatus(ApprovalStatus.PENDING);
    }
    
    public ProviderBuilder withRejectedApproval() {
        return withAllApprovalStatus(ApprovalStatus.REJECTED);
    }
    
    // Contact Person Methods
    public ProviderBuilder withContactPerson(ContactPerson contactPerson) {
        this.contactPerson = contactPerson;
        return this;
    }
    
    public ProviderBuilder withContactPerson(String name, String email, String position) {
        this.contactPerson = ContactPerson.builder()
            .name(name)
            .email(email)
            .phoneNumber("+94771234567")
            .position(position)
            .build();
        return this;
    }
    
    public ProviderBuilder withContactPerson(String name, String email, String phoneNumber, String position) {
        this.contactPerson = ContactPerson.builder()
            .name(name)
            .email(email)
            .phoneNumber(phoneNumber)
            .position(position)
            .build();
        return this;
    }
    
    public ProviderBuilder withContactPersonAndDocument(String name, String email, String position, String documentUrl) {
        this.contactPerson = ContactPerson.builder()
            .name(name)
            .email(email)
            .phoneNumber("+94771234567")
            .position(position)
            .identityDocumentUrl(documentUrl)
            .build();
        return this;
    }
    
    // =================
    // Build Method
    // =================
    
    public Provider build() {
        Provider provider = new Provider();
        
        // Set User fields through UserBuilder
        var user = userBuilder.build();
        provider.setUserId(user.getUserId());
        provider.setEmail(user.getEmail());
        provider.setPassword(user.getPassword());
        provider.setRole(user.getRole());
        provider.setStatus(user.getStatus());
        provider.setEmailVerified(user.getEmailVerified());
        provider.setCreatedAt(user.getCreatedAt());
        provider.setUpdatedAt(user.getUpdatedAt());
        
        // Set Provider-specific fields
        provider.setBusinessName(businessName);
        provider.setBusinessDescription(businessDescription);
        provider.setBusinessType(businessType);
        provider.setCoverImageUrl(coverImageUrl);
        provider.setBusinessRegistrationNumber(businessRegistrationNumber);
        provider.setBusinessRegistrationUrl(businessRegistrationUrl);
        provider.setStripeAccountId(stripeAccountId);
        provider.setAccommodationApprovalStatus(accommodationApprovalStatus);
        provider.setTourGuideApprovalStatus(tourGuideApprovalStatus);
        provider.setTransportApprovalStatus(transportApprovalStatus);
        provider.setActivityApprovalStatus(activityApprovalStatus);
        provider.setFoodApprovalStatus(foodApprovalStatus);
        provider.setContactPerson(contactPerson);
        
        return provider;
    }
}