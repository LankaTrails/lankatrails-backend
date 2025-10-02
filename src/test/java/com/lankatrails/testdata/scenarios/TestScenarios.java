package com.lankatrails.testdata.scenarios;

import com.lankatrails.lankatrails_backend.dtos.request.LoginRequest;
import com.lankatrails.lankatrails_backend.dtos.request.TouristRegistrationRequest;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.LoginResponse;
import com.lankatrails.lankatrails_backend.model.Admin;
import com.lankatrails.lankatrails_backend.model.Provider;
import com.lankatrails.lankatrails_backend.model.Tourist;
import com.lankatrails.testdata.builders.*;

/**
 * Comprehensive test scenario builder that provides ready-to-use combinations
 * of test data for common business scenarios across the LankaTrails application.
 * 
 * This class serves as a high-level facade over individual builders, providing
 * business-meaningful test data combinations that can be shared across all test classes.
 * 
 * Usage Examples:
 * <pre>
 * // User registration scenarios
 * TouristRegistrationRequest validRegistration = TestScenarios.validTouristRegistration();
 * TouristRegistrationRequest invalidRegistration = TestScenarios.invalidTouristRegistration();
 * 
 * // User entities for different states
 * Tourist verifiedTourist = TestScenarios.verifiedTourist();
 * Provider pendingProvider = TestScenarios.pendingProvider();
 * Admin systemAdmin = TestScenarios.systemAdmin();
 * 
 * // Authentication scenarios
 * LoginRequest validLogin = TestScenarios.validLoginCredentials();
 * LoginResponse successfulLogin = TestScenarios.successfulLoginResponse();
 * APIResponse<LoginResponse> loginError = TestScenarios.loginErrorResponse();
 * 
 * // Business-specific scenarios
 * Provider accommodationProvider = TestScenarios.approvedAccommodationProvider();
 * Tourist internationalTourist = TestScenarios.internationalTourist();
 * </pre>
 */
public class TestScenarios {
    
    // =================
    // Tourist Scenarios
    // =================
    
    /**
     * Creates a verified tourist with complete profile
     */
    public static Tourist verifiedTourist() {
        return TouristBuilder.aTourist()
            .withName("John", "Doe")
            .withEmail("john.doe@example.com")
            .withCountry("United States")
            .withPhoneNumber("+1234567890")
            .withVerifiedEmail()
            .withActiveStatus()
            .build();
    }
    
    /**
     * Creates a pending tourist awaiting email verification
     */
    public static Tourist pendingTourist() {
        return TouristBuilder.aTourist()
            .withName("Jane", "Smith")
            .withEmail("jane.smith@example.com")
            .withUnverifiedEmail()
            .withPendingStatus()
            .build();
    }
    
    /**
     * Creates an international tourist with typical profile
     */
    public static Tourist internationalTourist() {
        return TouristBuilder.anInternationalTourist()
            .withName("Emma", "Wilson")
            .withCountry("United Kingdom")
            .withPhoneNumber("+447123456789")
            .withVerifiedEmail()
            .build();
    }
    
    /**
     * Creates a local Sri Lankan tourist
     */
    public static Tourist localTourist() {
        return TouristBuilder.aLocalTourist()
            .withName("Kasun", "Perera")
            .withEmail("kasun.perera@gmail.com")
            .withCountry("Sri Lanka")
            .withPhoneNumber("+94771234567")
            .build();
    }
    
    // =================
    // Provider Scenarios
    // =================
    
    /**
     * Creates a pending provider awaiting approval
     */
    public static Provider pendingProvider() {
        return ProviderBuilder.aPendingProvider()
            .withBusinessName("New Lanka Tours")
            .withEmail("info@newlankatours.com")
            .withContactPerson("Rajesh Kumar", "rajesh@newlankatours.com", "Manager")
            .build();
    }
    
    /**
     * Creates an approved accommodation provider
     */
    public static Provider approvedAccommodationProvider() {
        return ProviderBuilder.anAccommodationProvider()
            .withBusinessName("Paradise Beach Resort")
            .withEmail("info@paradisebeach.lk")
            .withVerifiedEmail()
            .withStatus(com.lankatrails.lankatrails_backend.model.enums.UserStatus.ACTIVE)
            .withApprovedAccommodation()
            .withContactPerson("Priya Fernando", "priya@paradisebeach.lk", "Operations Manager")
            .build();
    }
    
    /**
     * Creates an approved tour guide provider
     */
    public static Provider approvedTourGuideProvider() {
        return ProviderBuilder.aTourGuideProvider()
            .withBusinessName("Cultural Heritage Tours")
            .withEmail("guide@heritage.lk")
            .withVerifiedEmail()
            .withStatus(com.lankatrails.lankatrails_backend.model.enums.UserStatus.ACTIVE)
            .withApprovedTourGuide()
            .build();
    }
    
    /**
     * Creates a multi-service provider with all approvals
     */
    public static Provider fullServiceProvider() {
        return ProviderBuilder.aCompanyProvider()
            .withBusinessName("Lanka Complete Tours Ltd")
            .withEmail("info@lankacomplete.com")
            .withVerifiedEmail()
            .withStatus(com.lankatrails.lankatrails_backend.model.enums.UserStatus.ACTIVE)
            .withAllServicesApproved()
            .withContactPerson("Sunil Rodrigo", "sunil@lankacomplete.com", "CEO")
            .build();
    }
    
    /**
     * Creates a rejected provider
     */
    public static Provider rejectedProvider() {
        return ProviderBuilder.aRejectedProvider()
            .withBusinessName("Rejected Tours")
            .withEmail("rejected@example.com")
            .withRejectedApproval()
            .build();
    }
    
    // =================
    // Admin Scenarios
    // =================
    
    /**
     * Creates a system administrator
     */
    public static Admin systemAdmin() {
        return AdminBuilder.aSystemAdmin()
            .withActiveStatus()
            .withVerifiedEmail()
            .build();
    }
    
    /**
     * Creates a senior admin with full privileges
     */
    public static Admin seniorAdmin() {
        return AdminBuilder.aSeniorAdmin()
            .withName("Sarah", "Johnson")
            .withEmail("sarah.johnson@lankatrails.com")
            .withActiveStatus()
            .build();
    }
    
    /**
     * Creates a support admin for customer service
     */
    public static Admin supportAdmin() {
        return AdminBuilder.aSupportAdmin()
            .withActiveStatus()
            .withVerifiedEmail()
            .build();
    }
    
    // =================
    // Registration Request Scenarios
    // =================
    
    /**
     * Creates a valid tourist registration request
     */
    public static TouristRegistrationRequest validTouristRegistration() {
        return TouristRegistrationRequestBuilder.aValidTouristRequest()
            .withName("Michael", "Brown")
            .withEmail("michael.brown@example.com")
            .withCountry("Canada")
            .withPhoneNumber("+1234567890")
            .withStrongPassword()
            .build();
    }
    
    /**
     * Creates an invalid tourist registration request for validation testing
     */
    public static TouristRegistrationRequest invalidTouristRegistration() {
        return TouristRegistrationRequestBuilder.anInvalidTouristRequest()
            .withInvalidEmail("invalid-email")
            .withWeakPassword("123")
            .withLongFirstName()
            .build();
    }
    
    /**
     * Creates a tourist registration with empty fields
     */
    public static TouristRegistrationRequest emptyTouristRegistration() {
        return TouristRegistrationRequestBuilder.aTouristRegistrationRequest()
            .withEmptyFields()
            .build();
    }
    
    // =================
    // Login Request Scenarios
    // =================
    
    /**
     * Creates valid login credentials for a tourist
     */
    public static LoginRequest validLoginCredentials() {
        return LoginRequestBuilder.aValidLoginRequest()
            .withEmail("john.doe@example.com")
            .withPassword("Password123")
            .build();
    }
    
    /**
     * Creates login credentials for an existing provider
     */
    public static LoginRequest providerLoginCredentials() {
        return LoginRequestBuilder.aProviderLoginRequest()
            .withExistingProviderCredentials()
            .build();
    }
    
    /**
     * Creates login credentials for an admin
     */
    public static LoginRequest adminLoginCredentials() {
        return LoginRequestBuilder.anAdminLoginRequest()
            .withExistingAdminCredentials()
            .build();
    }
    
    /**
     * Creates invalid login credentials for error testing
     */
    public static LoginRequest invalidLoginCredentials() {
        return LoginRequestBuilder.anInvalidLoginRequest()
            .withInvalidEmail()
            .withEmptyPassword()
            .build();
    }
    
    /**
     * Creates login credentials for non-existent user
     */
    public static LoginRequest nonExistentUserCredentials() {
        return LoginRequestBuilder.aLoginRequest()
            .withNonExistentUserCredentials()
            .build();
    }
    
    /**
     * Creates login credentials with wrong password
     */
    public static LoginRequest wrongPasswordCredentials() {
        return LoginRequestBuilder.aLoginRequest()
            .withWrongPasswordCredentials()
            .build();
    }
    
    // =================
    // Response Scenarios
    // =================
    
    /**
     * Creates a successful login response for a tourist
     */
    public static LoginResponse successfulLoginResponse() {
        return LoginResponseBuilder.aTouristLoginResponse()
            .withRegularLoginScenario()
            .withEmail("john.doe@example.com")
            .build();
    }
    
    /**
     * Creates a successful login response for a provider
     */
    public static LoginResponse providerLoginResponse() {
        return LoginResponseBuilder.aProviderLoginResponse()
            .withRegularLoginScenario()
            .withEmail("provider@business.com")
            .build();
    }
    
    /**
     * Creates a successful login response for an admin
     */
    public static LoginResponse adminLoginResponse() {
        return LoginResponseBuilder.anAdminLoginResponse()
            .withRegularLoginScenario()
            .withEmail("admin@lankatrails.com")
            .build();
    }
    
    /**
     * Creates an API success response for registration
     */
    public static APIResponse<String> registrationSuccessResponse() {
        return APIResponseBuilder.<String>aSuccessResponse()
            .withRegistrationSuccessMessage()
            .withData("User registered successfully")
            .build();
    }
    
    /**
     * Creates an API error response for login failure
     */
    public static APIResponse<Void> loginErrorResponse() {
        return APIResponseBuilder.<Void>anErrorResponse()
            .withLoginFailureMessage()
            .withDetails("Invalid email or password")
            .build();
    }
    
    /**
     * Creates an API validation error response
     */
    public static APIResponse<Object> validationErrorResponse() {
        return APIResponseBuilder.aValidationErrorResponse()
            .withMessage("Validation failed")
            .withDetails("Email format is invalid")
            .build();
    }
    
    /**
     * Creates an API unauthorized error response
     */
    public static APIResponse<Void> unauthorizedErrorResponse() {
        return APIResponseBuilder.<Void>anUnauthorizedErrorResponse()
            .withMessage("Access denied")
            .build();
    }
    
    // =================
    // Complex Business Scenarios
    // =================
    
    /**
     * Creates a complete user journey scenario with matching request/response
     */
    public static class UserJourneyScenario {
        public final TouristRegistrationRequest registrationRequest;
        public final Tourist registeredTourist;
        public final LoginRequest loginRequest;
        public final LoginResponse loginResponse;
        
        public UserJourneyScenario() {
            this.registrationRequest = validTouristRegistration();
            this.registeredTourist = verifiedTourist();
            this.loginRequest = validLoginCredentials();
            this.loginResponse = successfulLoginResponse();
        }
    }
    
    /**
     * Creates a complete provider approval scenario
     */
    public static class ProviderApprovalScenario {
        public final Provider pendingProvider;
        public final Provider approvedProvider;
        public final Admin approvingAdmin;
        
        public ProviderApprovalScenario() {
            this.pendingProvider = pendingProvider();
            this.approvedProvider = approvedAccommodationProvider();
            this.approvingAdmin = systemAdmin();
        }
    }
    
    /**
     * Creates a complete user journey scenario
     */
    public static UserJourneyScenario completeUserJourney() {
        return new UserJourneyScenario();
    }
    
    /**
     * Creates a complete provider approval scenario
     */
    public static ProviderApprovalScenario providerApprovalJourney() {
        return new ProviderApprovalScenario();
    }
}