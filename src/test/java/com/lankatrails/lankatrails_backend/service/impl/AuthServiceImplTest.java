package com.lankatrails.lankatrails_backend.service.impl;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import com.lankatrails.lankatrails_backend.dtos.request.ContactPersonDTO;
import com.lankatrails.lankatrails_backend.dtos.request.LicenseDTO;
import com.lankatrails.lankatrails_backend.dtos.request.LocationDTO;
import com.lankatrails.lankatrails_backend.dtos.request.LoginRequest;
import com.lankatrails.lankatrails_backend.dtos.request.ProviderRegistrationRequest;
import com.lankatrails.lankatrails_backend.dtos.request.TouristRegistrationRequest;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.LoginResponse;
import com.lankatrails.lankatrails_backend.dtos.response.RegistrationResponse;
import com.lankatrails.lankatrails_backend.exception.BadCredentialsException;
import com.lankatrails.lankatrails_backend.exception.BadRequestException;
import com.lankatrails.lankatrails_backend.exception.EmailAlreadyExistsException;
import com.lankatrails.lankatrails_backend.exception.UnauthorizedException;
import com.lankatrails.lankatrails_backend.exception.UserNotFoundException;
import com.lankatrails.lankatrails_backend.exception.UserPendingApprovalException;
import com.lankatrails.lankatrails_backend.factory.UserFactory;
import com.lankatrails.lankatrails_backend.model.Admin;
import com.lankatrails.lankatrails_backend.model.Provider;
import com.lankatrails.lankatrails_backend.model.Tourist;
import com.lankatrails.lankatrails_backend.model.User;
import com.lankatrails.lankatrails_backend.model.enums.BusinessType;
import com.lankatrails.lankatrails_backend.model.enums.ServiceCategory;
import com.lankatrails.lankatrails_backend.model.enums.UploadCategory;
import com.lankatrails.lankatrails_backend.model.enums.UserRole;
import com.lankatrails.lankatrails_backend.model.enums.UserStatus;
import com.lankatrails.lankatrails_backend.repositories.AdminRepository;
import com.lankatrails.lankatrails_backend.repositories.LicenseRepository;
import com.lankatrails.lankatrails_backend.repositories.ProviderRepository;
import com.lankatrails.lankatrails_backend.repositories.TouristRepository;
import com.lankatrails.lankatrails_backend.repositories.UserRepository;
import com.lankatrails.lankatrails_backend.security.jwt.JwtUtils;
import com.lankatrails.lankatrails_backend.security.service.RefreshTokenRedisService;
import com.lankatrails.lankatrails_backend.security.service.UserDetailsImpl;
import com.lankatrails.lankatrails_backend.security.utils.AuthUtils;
import com.lankatrails.lankatrails_backend.service.utils.EmailService;
import com.lankatrails.lankatrails_backend.service.utils.FileUploadService;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Comprehensive test suite for AuthServiceImpl using BDD-style organization
 * 
 * This test class is organized around user journeys and follows Given-When-Then naming conventions.
 * Each major feature is grouped into logical nested classes representing real user scenarios.
 * 
 * Test Structure:
 * - User Registration Journey: Tourist & Provider registration scenarios
 * - Authentication Journey: Login, logout, and security scenarios  
 * - Token Management Journey: JWT and refresh token operations
 * - Account Management Journey: Email verification, password changes, profiles
 * - Admin Workflow Journey: Provider approval processes
 * - System Reliability Journey: Edge cases, error handling, boundary conditions
 * 
 * @author LankaTrails Development Team
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Implementation - User Journey Tests")
class AuthServiceImplTest {

    // =====================================================
    // MOCK DEPENDENCIES
    // =====================================================
    
    @Mock private UserRepository userRepository;
    @Mock private TouristRepository touristRepository;
    @Mock private ProviderRepository providerRepository;
    @Mock private AdminRepository adminRepository;
    @Mock private LicenseRepository licenseRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private UserFactory userFactory;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtUtils jwtUtils;
    @Mock private AuthUtils authUtills;
    @Mock private RefreshTokenRedisService refreshTokenRedisService;
    @Mock private FileUploadService fileUploadService;
    @Mock private EmailService emailService;
    @Mock private ModelMapper modelMapper;
    @Mock private HttpServletRequest httpServletRequest;
    @Mock private MultipartFile multipartFile;

    // =====================================================
    // TEST FIXTURES
    // =====================================================
    
    private AuthServiceImpl authService;
    private User testUser;
    private Tourist testTourist;
    private Provider testProvider;
    private Admin testAdmin;
    private UserDetailsImpl userDetails;

    // =====================================================
    // TEST DATA BUILDERS
    // =====================================================
    
    /**
     * Test Data Builder for creating test entities and DTOs with fluent API
     */
    static class TestDataBuilder {
        
        static TouristRegistrationRequestBuilder aTouristRegistration() {
            return new TouristRegistrationRequestBuilder();
        }
        
        static ProviderRegistrationRequestBuilder aProviderRegistration() {
            return new ProviderRegistrationRequestBuilder();
        }
        
        static UserBuilder aUser() {
            return new UserBuilder();
        }
        
        static LoginRequestBuilder aLoginRequest() {
            return new LoginRequestBuilder();
        }
        
        static class TouristRegistrationRequestBuilder {
            private TouristRegistrationRequest request = new TouristRegistrationRequest();
            
            public TouristRegistrationRequestBuilder withEmail(String email) {
                request.setEmail(email);
                return this;
            }
            
            public TouristRegistrationRequestBuilder withPassword(String password) {
                request.setPassword(password);
                return this;
            }
            
            public TouristRegistrationRequestBuilder withName(String firstName, String lastName) {
                request.setFirstName(firstName);
                request.setLastName(lastName);
                return this;
            }
            
            public TouristRegistrationRequestBuilder withCountry(String country) {
                request.setCountry(country);
                return this;
            }
            
            public TouristRegistrationRequestBuilder withPhoneNumber(String phoneNumber) {
                request.setPhoneNumber(phoneNumber);
                return this;
            }
            
            public TouristRegistrationRequest build() {
                return request;
            }
        }
        
        static class ProviderRegistrationRequestBuilder {
            private ProviderRegistrationRequest request = new ProviderRegistrationRequest();
            
            public ProviderRegistrationRequestBuilder withEmail(String email) {
                request.setEmail(email);
                return this;
            }
            
            public ProviderRegistrationRequestBuilder withPassword(String password) {
                request.setPassword(password);
                return this;
            }
            
            public ProviderRegistrationRequestBuilder withBusinessName(String businessName) {
                request.setBusinessName(businessName);
                return this;
            }
            
            public ProviderRegistrationRequestBuilder withBusinessType(BusinessType businessType) {
                request.setBusinessType(businessType);
                return this;
            }
            
            public ProviderRegistrationRequestBuilder withContactPerson(String name, String email, String phone, String position) {
                ContactPersonDTO contactPerson = new ContactPersonDTO();
                contactPerson.setName(name);
                contactPerson.setEmail(email);
                contactPerson.setPhoneNumber(phone);
                contactPerson.setPosition(position);
                request.setContactPerson(contactPerson);
                return this;
            }
            
            public ProviderRegistrationRequestBuilder withLocation(String city) {
                LocationDTO location = new LocationDTO();
                location.setCity(city);
                request.setLocation(location);
                return this;
            }
            
            public ProviderRegistrationRequestBuilder withLicense(String licenseNumber, LocalDate expiryDate, ServiceCategory category) {
                LicenseDTO license = new LicenseDTO();
                license.setLicenseNumber(licenseNumber);
                license.setExpiryDate(expiryDate);
                license.setCategory(category);
                request.setLicenses(List.of(license));
                return this;
            }
            
            public ProviderRegistrationRequest build() {
                return request;
            }
        }
        
        static class UserBuilder {
            private User user = new User();
            
            public UserBuilder withId(Long id) {
                user.setUserId(id);
                return this;
            }
            
            public UserBuilder withEmail(String email) {
                user.setEmail(email);
                return this;
            }
            
            public UserBuilder withPassword(String password) {
                user.setPassword(password);
                return this;
            }
            
            public UserBuilder withRole(UserRole role) {
                user.setRole(role);
                return this;
            }
            
            public UserBuilder withStatus(UserStatus status) {
                user.setStatus(status);
                return this;
            }
            
            public UserBuilder withEmailVerified(boolean verified) {
                user.setEmailVerified(verified);
                return this;
            }
            
            public User build() {
                return user;
            }
        }
        
        static class LoginRequestBuilder {
            private LoginRequest request = new LoginRequest();
            
            public LoginRequestBuilder withEmail(String email) {
                request.setEmail(email);
                return this;
            }
            
            public LoginRequestBuilder withPassword(String password) {
                request.setPassword(password);
                return this;
            }
            
            public LoginRequest build() {
                return request;
            }
        }
    }
    
    /**
     * Scenario-specific builders for common test scenarios
     */
    static class ScenarioBuilder {
        
        static TestDataBuilder.UserBuilder anUnverifiedUser() {
            return TestDataBuilder.aUser()
                    .withId(1L)
                    .withEmail("unverified@example.com")
                    .withPassword("encodedPassword")
                    .withRole(UserRole.ROLE_TOURIST)
                    .withStatus(UserStatus.ACTIVE)
                    .withEmailVerified(false);
        }
        
        static TestDataBuilder.UserBuilder aPendingProvider() {
            return TestDataBuilder.aUser()
                    .withId(2L)
                    .withEmail("pending@example.com")
                    .withPassword("encodedPassword")
                    .withRole(UserRole.ROLE_PROVIDER)
                    .withStatus(UserStatus.PENDING)
                    .withEmailVerified(true);
        }
        
        static TestDataBuilder.UserBuilder anActiveAdmin() {
            return TestDataBuilder.aUser()
                    .withId(3L)
                    .withEmail("admin@example.com")
                    .withPassword("encodedPassword")
                    .withRole(UserRole.ROLE_ADMIN)
                    .withStatus(UserStatus.ACTIVE)
                    .withEmailVerified(true);
        }
        
        static TestDataBuilder.TouristRegistrationRequestBuilder aValidTouristRegistration() {
            return TestDataBuilder.aTouristRegistration()
                    .withEmail("tourist@example.com")
                    .withPassword("password123")
                    .withName("Jane", "Smith")
                    .withCountry("Canada")
                    .withPhoneNumber("9876543210");
        }
        
        static TestDataBuilder.ProviderRegistrationRequestBuilder aValidProviderRegistration() {
            return TestDataBuilder.aProviderRegistration()
                    .withEmail("provider@example.com")
                    .withPassword("password123")
                    .withBusinessName("Test Business")
                    .withBusinessType(BusinessType.INDIVIDUAL)
                    .withContactPerson("Contact Person", "contact@example.com", "1234567890", "Manager")
                    .withLocation("Test City")
                    .withLicense("LIC123", LocalDate.now().plusYears(1), ServiceCategory.ACCOMMODATION);
        }
    }

    // =====================================================
    // TEST SETUP
    // =====================================================
    
    @BeforeEach
    void setUp() throws Exception {
        // Create AuthServiceImpl instance manually with constructor dependencies
        authService = new AuthServiceImpl(
                userRepository,
                touristRepository,
                providerRepository,
                adminRepository,
                licenseRepository,
                passwordEncoder,
                userFactory
        );
        
        // Set the non-final dependencies using reflection
        setPrivateField(authService, "authenticationManager", authenticationManager);
        setPrivateField(authService, "jwtUtils", jwtUtils);
        setPrivateField(authService, "authUtills", authUtills);
        setPrivateField(authService, "refreshTokenRedisService", refreshTokenRedisService);
        setPrivateField(authService, "fileUploadService", fileUploadService);
        setPrivateField(authService, "emailService", emailService);
        setPrivateField(authService, "modelMapper", modelMapper);
        
        // Setup test user entities
        testUser = TestDataBuilder.aUser()
                .withId(1L)
                .withEmail("test@example.com")
                .withPassword("encodedPassword")
                .withRole(UserRole.ROLE_TOURIST)
                .withStatus(UserStatus.ACTIVE)
                .withEmailVerified(true)
                .build();

        testTourist = new Tourist();
        testTourist.setUserId(1L);
        testTourist.setFirstName("John");
        testTourist.setLastName("Doe");
        testTourist.setCountry("USA");
        testTourist.setPhoneNumber("1234567890");

        testProvider = new Provider();
        testProvider.setUserId(2L);
        testProvider.setEmail("provider@example.com");
        testProvider.setRole(UserRole.ROLE_PROVIDER);
        testProvider.setStatus(UserStatus.ACTIVE);
        testProvider.setEmailVerified(true);
        testProvider.setBusinessName("Test Business");
        testProvider.setBusinessType(BusinessType.INDIVIDUAL);

        testAdmin = new Admin();
        testAdmin.setUserId(3L);
        testAdmin.setFirstName("Admin");
        testAdmin.setLastName("User");

        // Setup UserDetails
        userDetails = new UserDetailsImpl(
                1L,
                "test@example.com",
                "encodedPassword",
                true,
                UserRole.ROLE_TOURIST,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_TOURIST"))
        );
    }

    // =====================================================
    // USER REGISTRATION JOURNEY
    // =====================================================
    
    @Nested
    @DisplayName("Given User Registration Requirements - User Registration Journey")
    class UserRegistrationJourney {
        
        @Nested
        @DisplayName("Tourist Registration Scenarios")
        class TouristRegistrationScenarios {

            @Test
            @DisplayName("Given valid tourist data, when registering tourist, then registration succeeds")
            void givenValidTouristData_whenRegisteringTourist_thenRegistrationSucceeds() {
                // Arrange
                TouristRegistrationRequest request = ScenarioBuilder.aValidTouristRegistration().build();
                when(userRepository.existsByEmail(anyString())).thenReturn(false);
                when(userFactory.createUser(any(TouristRegistrationRequest.class))).thenReturn(testUser);
                when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
                when(userRepository.save(any(User.class))).thenReturn(testUser);
                when(jwtUtils.generateEmailVerificationJwt(any(User.class))).thenReturn("verificationToken");
                doNothing().when(emailService).sendEmail(anyString(), anyString(), anyString(), any());

                // Act
                APIResponse<RegistrationResponse> result = authService.registerTourist(request);

                // Assert
                assertThat(result).isNotNull();
                assertThat(result.isSuccess()).isTrue();
                assertThat(result.getMessage()).isEqualTo("Tourist registered successfully.");
                assertThat(result.getData()).isNotNull();
                assertThat(result.getData().getEmail()).isEqualTo(testUser.getEmail());
                assertThat(result.getData().getRole()).isEqualTo(UserRole.ROLE_TOURIST);

                verify(userRepository).existsByEmail("tourist@example.com");
                verify(userFactory).createUser(request);
                verify(passwordEncoder).encode("password123");
                verify(userRepository).save(any(User.class));
                verify(emailService).sendEmail(anyString(), anyString(), anyString(), any());
            }

            @Test
            @DisplayName("Given existing email, when registering tourist, then throws EmailAlreadyExistsException")
            void givenExistingEmail_whenRegisteringTourist_thenThrowsEmailAlreadyExistsException() {
                // Arrange
                TouristRegistrationRequest request = ScenarioBuilder.aValidTouristRegistration().build();
                when(userRepository.existsByEmail(anyString())).thenReturn(true);

                // Act & Assert
                assertThatThrownBy(() -> authService.registerTourist(request))
                        .isInstanceOf(EmailAlreadyExistsException.class)
                        .hasMessageContaining("tourist@example.com");

                verify(userRepository).existsByEmail("tourist@example.com");
                verifyNoInteractions(userFactory, passwordEncoder, emailService);
            }

            @Test
            @DisplayName("Given null request, when registering tourist, then throws NullPointerException")
            void givenNullRequest_whenRegisteringTourist_thenThrowsNullPointerException() {
                // Act & Assert
                assertThatThrownBy(() -> authService.registerTourist(null))
                        .isInstanceOf(NullPointerException.class);
            }

            @Test
            @DisplayName("Given empty email, when registering tourist, then throws RuntimeException")
            void givenEmptyEmail_whenRegisteringTourist_thenThrowsRuntimeException() {
                // Arrange
                TouristRegistrationRequest request = TestDataBuilder.aTouristRegistration()
                        .withEmail("")
                        .withPassword("password123")
                        .withName("John", "Doe")
                        .withCountry("USA")
                        .withPhoneNumber("1234567890")
                        .build();

                // Act & Assert
                assertThatThrownBy(() -> authService.registerTourist(request))
                        .isInstanceOf(RuntimeException.class);
            }
        }

        @Nested
        @DisplayName("Provider Registration Scenarios")
        class ProviderRegistrationScenarios {

            @Test
            @DisplayName("Given valid provider data with files, when registering provider, then registration succeeds")
            void givenValidProviderDataWithFiles_whenRegisteringProvider_thenRegistrationSucceeds() {
                // Arrange
                ProviderRegistrationRequest request = ScenarioBuilder.aValidProviderRegistration().build();
                when(userRepository.existsByEmail(anyString())).thenReturn(false);
                when(multipartFile.isEmpty()).thenReturn(false);
                when(fileUploadService.storeFile(any(MultipartFile.class), any(UploadCategory.class), any()))
                        .thenReturn("file-url");
                when(userFactory.createUser(any(ProviderRegistrationRequest.class))).thenReturn(testProvider);
                when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
                when(userRepository.save(any(User.class))).thenReturn(testProvider);
                when(jwtUtils.generateEmailVerificationJwt(any(User.class))).thenReturn("verificationToken");
                doNothing().when(emailService).sendEmail(anyString(), anyString(), anyString(), any());

                // Act
                APIResponse<RegistrationResponse> result = authService.registerProvider(
                        request, multipartFile, multipartFile, multipartFile, multipartFile, List.of(multipartFile)
                );

                // Assert
                assertThat(result).isNotNull();
                assertThat(result.isSuccess()).isTrue();
                assertThat(result.getMessage()).isEqualTo("Provider registered successfully.");
                assertThat(result.getData()).isNotNull();
                assertThat(result.getData().getRole()).isEqualTo(UserRole.ROLE_PROVIDER);

                verify(userRepository).existsByEmail("provider@example.com");
                verify(fileUploadService, times(5)).storeFile(any(MultipartFile.class), any(UploadCategory.class), any());
                verify(userRepository).save(any(User.class));
            }

            @Test
            @DisplayName("Given missing business registration file, when registering provider, then throws BadRequestException")
            void givenMissingBusinessRegistrationFile_whenRegisteringProvider_thenThrowsBadRequestException() {
                // Arrange
                ProviderRegistrationRequest request = ScenarioBuilder.aValidProviderRegistration().build();
                when(userRepository.existsByEmail(anyString())).thenReturn(false);
                when(multipartFile.isEmpty()).thenReturn(true);

                // Act & Assert
                assertThatThrownBy(() -> authService.registerProvider(
                        request, null, null, multipartFile, null, null
                ))
                        .isInstanceOf(BadRequestException.class)
                        .hasMessageContaining("Business registration file is required");
            }

            @Test
            @DisplayName("Given missing contact person file, when registering provider, then throws BadRequestException")
            void givenMissingContactPersonFile_whenRegisteringProvider_thenThrowsBadRequestException() {
                // Arrange
                ProviderRegistrationRequest request = ScenarioBuilder.aValidProviderRegistration().build();
                when(userRepository.existsByEmail(anyString())).thenReturn(false);
                when(multipartFile.isEmpty()).thenReturn(false).thenReturn(true);
                when(fileUploadService.storeFile(any(MultipartFile.class), any(UploadCategory.class), any()))
                        .thenReturn("file-url");

                // Act & Assert
                assertThatThrownBy(() -> authService.registerProvider(
                        request, null, null, multipartFile, multipartFile, null
                ))
                        .isInstanceOf(BadRequestException.class)
                        .hasMessageContaining("Contact person identity file is required");
            }

            @Test
            @DisplayName("Given expired license, when registering provider, then throws BadRequestException")
            void givenExpiredLicense_whenRegisteringProvider_thenThrowsBadRequestException() {
                // Arrange
                ProviderRegistrationRequest request = TestDataBuilder.aProviderRegistration()
                        .withEmail("provider@example.com")
                        .withPassword("password123")
                        .withBusinessName("Test Business")
                        .withBusinessType(BusinessType.INDIVIDUAL)
                        .withContactPerson("Contact Person", "contact@example.com", "1234567890", "Manager")
                        .withLocation("Test City")
                        .withLicense("EXP123", LocalDate.now().minusDays(1), ServiceCategory.ACCOMMODATION) // Expired
                        .build();
                
                when(userRepository.existsByEmail(anyString())).thenReturn(false);
                when(multipartFile.isEmpty()).thenReturn(false);
                when(fileUploadService.storeFile(any(MultipartFile.class), any(UploadCategory.class), any()))
                        .thenReturn("file-url");

                // Act & Assert
                assertThatThrownBy(() -> authService.registerProvider(
                        request, null, null, multipartFile, multipartFile, List.of(multipartFile)
                ))
                        .isInstanceOf(BadRequestException.class)
                        .hasMessageContaining("expired");
            }
        }
    }

    // =====================================================
    // AUTHENTICATION JOURNEY  
    // =====================================================
    
    @Nested
    @DisplayName("Given Authentication Requirements - Authentication Journey")
    class AuthenticationJourney {
        
        @Nested
        @DisplayName("User Login Scenarios")
        class UserLoginScenarios {

            @Test
            @DisplayName("Given valid credentials, when authenticating user, then returns login response")
            void givenValidCredentials_whenAuthenticatingUser_thenReturnsLoginResponse() {
                // Arrange
                LoginRequest request = TestDataBuilder.aLoginRequest()
                        .withEmail("test@example.com")
                        .withPassword("password123")
                        .build();
                        
                Authentication authentication = mock(Authentication.class);
                when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
                when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                        .thenReturn(authentication);
                when(authentication.getPrincipal()).thenReturn(userDetails);
                when(jwtUtils.generateToken(any(UserDetailsImpl.class))).thenReturn("jwt-token");
                when(jwtUtils.generateRefreshToken(any(UserDetailsImpl.class))).thenReturn("refresh-token");
                doNothing().when(refreshTokenRedisService).storeToken(anyString(), anyString());

                // Act
                APIResponse<LoginResponse> result = authService.authenticateUser(request, httpServletRequest);

                // Assert
                assertThat(result).isNotNull();
                assertThat(result.isSuccess()).isTrue();
                assertThat(result.getMessage()).isEqualTo("User logged in successfully.");
                assertThat(result.getData()).isNotNull();
                assertThat(result.getData().getJwtToken()).isEqualTo("jwt-token");
                assertThat(result.getData().getRefreshToken()).isEqualTo("refresh-token");

                verify(userRepository).findByEmail("test@example.com");
                verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
                verify(refreshTokenRedisService).storeToken(anyString(), anyString());
            }

            @Test
            @DisplayName("Given non-existent user, when authenticating user, then throws UserNotFoundException")
            void givenNonExistentUser_whenAuthenticatingUser_thenThrowsUserNotFoundException() {
                // Arrange
                LoginRequest request = TestDataBuilder.aLoginRequest()
                        .withEmail("nonexistent@example.com")
                        .withPassword("password123")
                        .build();
                when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

                // Act & Assert
                assertThatThrownBy(() -> authService.authenticateUser(request, httpServletRequest))
                        .isInstanceOf(UserNotFoundException.class)
                        .hasMessageContaining("User not found with email");

                verify(userRepository).findByEmail("nonexistent@example.com");
                verifyNoInteractions(authenticationManager);
            }

            @Test
            @DisplayName("Given unverified email, when authenticating user, then throws BadCredentialsException")
            void givenUnverifiedEmail_whenAuthenticatingUser_thenThrowsBadCredentialsException() {
                // Arrange
                User unverifiedUser = ScenarioBuilder.anUnverifiedUser().build();
                LoginRequest request = TestDataBuilder.aLoginRequest()
                        .withEmail("unverified@example.com")
                        .withPassword("password123")
                        .build();
                when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(unverifiedUser));

                // Act & Assert
                assertThatThrownBy(() -> authService.authenticateUser(request, httpServletRequest))
                        .isInstanceOf(BadCredentialsException.class)
                        .hasMessageContaining("Please verify your email");

                verify(userRepository).findByEmail("unverified@example.com");
                verifyNoInteractions(authenticationManager);
            }

            @Test
            @DisplayName("Given non-tourist on mobile, when authenticating user, then throws UnauthorizedException")
            void givenNonTouristOnMobile_whenAuthenticatingUser_thenThrowsUnauthorizedException() {
                // Arrange
                User providerUser = TestDataBuilder.aUser()
                        .withId(1L)
                        .withEmail("provider@example.com")
                        .withPassword("encodedPassword")
                        .withRole(UserRole.ROLE_PROVIDER)
                        .withStatus(UserStatus.ACTIVE)
                        .withEmailVerified(true)
                        .build();
                        
                LoginRequest request = TestDataBuilder.aLoginRequest()
                        .withEmail("provider@example.com")
                        .withPassword("password123")
                        .build();
                        
                when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(providerUser));
                when(httpServletRequest.getHeader("Client-Type")).thenReturn("MOBILE");

                // Act & Assert
                assertThatThrownBy(() -> authService.authenticateUser(request, httpServletRequest))
                        .isInstanceOf(UnauthorizedException.class)
                        .hasMessageContaining("Only tourist accounts are allowed on the mobile app");

                verify(userRepository).findByEmail("provider@example.com");
                verify(httpServletRequest).getHeader("Client-Type");
            }

            @Test
            @DisplayName("Given invalid credentials, when authenticating user, then throws BadCredentialsException")
            void givenInvalidCredentials_whenAuthenticatingUser_thenThrowsBadCredentialsException() {
                // Arrange
                LoginRequest request = TestDataBuilder.aLoginRequest()
                        .withEmail("test@example.com")
                        .withPassword("wrongpassword")
                        .build();
                when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
                when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                        .thenThrow(new org.springframework.security.authentication.BadCredentialsException("Invalid credentials"));

                // Act & Assert
                assertThatThrownBy(() -> authService.authenticateUser(request, httpServletRequest))
                        .isInstanceOf(BadCredentialsException.class)
                        .hasMessageContaining("Invalid email or password");

                verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            }

            @Test
            @DisplayName("Given pending user, when authenticating user, then throws UserPendingApprovalException")
            void givenPendingUser_whenAuthenticatingUser_thenThrowsUserPendingApprovalException() {
                // Arrange
                User pendingUser = ScenarioBuilder.aPendingProvider().build();
                LoginRequest request = TestDataBuilder.aLoginRequest()
                        .withEmail("pending@example.com")
                        .withPassword("password123")
                        .build();
                        
                Authentication authentication = mock(Authentication.class);
                when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(pendingUser));
                when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                        .thenReturn(authentication);

                // Act & Assert
                assertThatThrownBy(() -> authService.authenticateUser(request, httpServletRequest))
                        .isInstanceOf(UserPendingApprovalException.class)
                        .hasMessageContaining("Your account is pending approval");

                verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            }
        }

        @Nested
        @DisplayName("User Logout Scenarios")
        class UserLogoutScenarios {

            @Test
            @DisplayName("Given valid token, when logging out, then logout succeeds")
            void givenValidToken_whenLoggingOut_thenLogoutSucceeds() {
                // Arrange
                when(authUtills.loggedInEmail()).thenReturn("test@example.com");
                doNothing().when(refreshTokenRedisService).deleteToken(anyString());

                // Act
                APIResponse<String> result = authService.logoutUser(httpServletRequest);

                // Assert
                assertThat(result).isNotNull();
                assertThat(result.isSuccess()).isTrue();
                assertThat(result.getMessage()).isEqualTo("User logged out successfully.");

                verify(authUtills).loggedInEmail();
                verify(refreshTokenRedisService).deleteToken("test@example.com");
            }

            @Test
            @DisplayName("Given invalid token, when logging out, then throws UnauthorizedException")
            void givenInvalidToken_whenLoggingOut_thenThrowsUnauthorizedException() {
                // Arrange
                when(authUtills.loggedInEmail()).thenReturn(null);

                // Act & Assert
                assertThatThrownBy(() -> authService.logoutUser(httpServletRequest))
                        .isInstanceOf(UnauthorizedException.class)
                        .hasMessageContaining("Email not found in JWT token");

                verify(authUtills).loggedInEmail();
                verifyNoInteractions(refreshTokenRedisService);
            }
        }
    }

    // =====================================================
    // UTILITY METHODS
    // =====================================================
    
    /**
     * Utility method to set private fields using reflection
     */
    private void setPrivateField(Object object, String fieldName, Object value) {
        try {
            var field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(object, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field: " + fieldName, e);
        }
    }
}