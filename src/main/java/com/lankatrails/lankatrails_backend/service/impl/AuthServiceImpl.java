package com.lankatrails.lankatrails_backend.service.impl;

import com.lankatrails.lankatrails_backend.dtos.request.*;
import com.lankatrails.lankatrails_backend.dtos.response.*;
import com.lankatrails.lankatrails_backend.exception.*;
import com.lankatrails.lankatrails_backend.factory.*;
import com.lankatrails.lankatrails_backend.model.*;
import com.lankatrails.lankatrails_backend.model.enums.UploadCategory;
import com.lankatrails.lankatrails_backend.model.enums.UserRole;
import com.lankatrails.lankatrails_backend.model.enums.UserStatus;
import com.lankatrails.lankatrails_backend.repositories.*;
import com.lankatrails.lankatrails_backend.security.jwt.JwtUtils;
import com.lankatrails.lankatrails_backend.security.service.RefreshTokenRedisService;
import com.lankatrails.lankatrails_backend.security.service.UserDetailsImpl;
import com.lankatrails.lankatrails_backend.security.utils.AuthUtils;
import com.lankatrails.lankatrails_backend.service.AuthService;
import com.lankatrails.lankatrails_backend.service.utils.EmailService;
import com.lankatrails.lankatrails_backend.service.utils.FileUploadService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final TouristRepository touristRepository;

    @Autowired
    private final ProviderRepository providerRepository;

    @Autowired
    private final AdminRepository adminRepository;

    @Autowired
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private final UserFactory userFactory;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private AuthUtils authUtills;

    @Autowired
    private RefreshTokenRedisService refreshTokenRedisService;

    @Autowired
    private FileUploadService fileUploadService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ModelMapper modelMapper;

    private void sendVerificationEmail(User user) {
        String jwtToken = jwtUtils.generateEmailVerificationJwt(user);

        String verificationUrl = String.format("http://localhost:8080/api/auth/verify-email?token=%s", jwtToken);

        Map<String, Object> params = new HashMap<>();
//        params.put("name", user.getFirstName());
        params.put("verificationUrl", verificationUrl);

        emailService.sendEmail(
                user.getEmail(),
                "Verify your email address",
                "emails/verify-email", // template path relative to templates dir
                params
        );
    }


    @Override
    @Transactional
    public APIResponse<RegistrationResponse> registerTourist(TouristRegistrationRequest request ) {
        log.info("Attempting tourist registration for email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail().toLowerCase())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        User user = userFactory.createUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Handle profile picture upload if provided
//        if (profilePicture != null && !profilePicture.isEmpty()) {
//            String profilePictureUrl = fileUploadService.storeFile(profilePicture, UploadCategory.PROFILE_PICTURE);
//            user.setProfilePictureUrl(profilePictureUrl);
//        }

        User savedUser = userRepository.save(user);
        log.info("Tourist registered successfully with ID: {}", savedUser.getUserId());

        sendVerificationEmail(savedUser);

        return APIResponse.<RegistrationResponse>builder()
                .success(true)
                .message("Tourist registered successfully.")
                .data(RegistrationResponse.builder()
                        .userId(savedUser.getUserId())
                        .email(savedUser.getEmail())
                        .role(savedUser.getRole())
                        .status(savedUser.getStatus())
                        .emailVerified(savedUser.getEmailVerified())
                        .build())
                .build();
    }

    @Override
    @Transactional
    public APIResponse<RegistrationResponse> registerProvider(ProviderRegistrationRequest request,
                                                              MultipartFile profilePicture,
                                                              MultipartFile coverPhoto,
                                                              MultipartFile businessRegistrationFile,
                                                              MultipartFile contactPersonIdentityFile,
                                                              List<MultipartFile> licenseFiles) {
        log.info("Attempting provider registration for email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail().toLowerCase())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        // Handle profile picture upload if provided
        if (profilePicture != null && !profilePicture.isEmpty()) {
            String profilePictureUrl = fileUploadService.storeFile(profilePicture, UploadCategory.PROFILE_PICTURE, null);
            log.info("Profile picture uploaded successfully: {}", profilePictureUrl);
            request.setProfilePictureUrl(profilePictureUrl);
        }

        // Handle cover photo upload if provided
        if (coverPhoto != null && !coverPhoto.isEmpty()) {
            String coverPhotoUrl = fileUploadService.storeFile(coverPhoto, UploadCategory.COVER_PICTURE, null);
            log.info("Cover photo uploaded successfully: {}", coverPhotoUrl);
            request.setCoverImageUrl(coverPhotoUrl);
        }

        // Handle business registration file upload
        if (businessRegistrationFile != null && !businessRegistrationFile.isEmpty()) {
            String businessRegistrationUrl = fileUploadService.storeFile(businessRegistrationFile, UploadCategory.BUSINESS_REGISTRATION, null);
            log.info("Business registration file uploaded successfully: {}", businessRegistrationUrl);
            request.setBusinessRegistrationUrl(businessRegistrationUrl);
        } else {
            log.warn("No business registration file provided for provider registration.");
            throw new BadRequestException("Business registration file is required for provider registration.", "businessRegistrationFile", null);
        }

        // Handle contact person identity file upload
        if (contactPersonIdentityFile != null && !contactPersonIdentityFile.isEmpty()) {
            String contactPersonIdentityUrl = fileUploadService.storeFile(contactPersonIdentityFile, UploadCategory.IDENTIFICATION, null);
            log.info("Contact person identity file uploaded successfully: {}", contactPersonIdentityUrl);
            request.getContactPerson().setIdentityDocumentUrl(contactPersonIdentityUrl);
        } else {
            log.warn("No contact person identity file provided for provider registration.");
            throw new BadRequestException("Contact person identity file is required for provider registration.", "contactPersonIdentityFile", null);
        }

        // Handle license files upload
        List<LicenseDTO> licenses = new ArrayList<>(request.getLicenses());
        for (int i = 0; i < licenseFiles.size(); i++) {
            LicenseDTO license = licenses.get(i);
            MultipartFile licenseFile = licenseFiles.get(i);

            license.setLicenseUrl(fileUploadService.storeFile(licenseFile, UploadCategory.LICENCE, license.getCategory().getDisplayName().toLowerCase()));
            log.info("License file uploaded successfully for license number {}: {}", license.getLicenseNumber(), license.getLicenseUrl());

            // Additional validation
            if (license.getExpiryDate().isBefore(LocalDate.now())) {
                throw new BadRequestException(
                        String.format("License %s expired on %s",
                                license.getLicenseNumber(),
                                license.getExpiryDate()),
                        "licenses[" + i + "].expiryDate",
                        null
                );
            }
        }

        User user = userFactory.createUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        User savedUser = userRepository.save(user);
        sendVerificationEmail(savedUser);
        log.info("Provider registered successfully with ID: {}", savedUser.getUserId());

        return APIResponse.<RegistrationResponse>builder()
                .success(true)
                .message("Provider registered successfully.")
                .data(RegistrationResponse.builder()
                        .userId(savedUser.getUserId())
                        .email(savedUser.getEmail())
                        .role(savedUser.getRole())
                        .status(savedUser.getStatus())
                        .emailVerified(savedUser.getEmailVerified())
                        .build())
                .build();
    }

    @Override
    public APIResponse<LoginResponse> authenticateUser(LoginRequest request, HttpServletRequest httpServletRequest) {
        log.info("Attempting login for email: {}", request.getEmail());

        // Fetch user by email to check if verified before authenticating
        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + request.getEmail()));

        // Check if email is verified
        if (!user.getEmailVerified()) {
            log.warn("Login failed: Email not verified for {}", request.getEmail());
            throw new BadCredentialsException("Please verify your email before logging in.");
        }

        String clientType = httpServletRequest.getHeader("Client-Type");

        if ("MOBILE".equalsIgnoreCase(clientType) && user.getRole() != UserRole.ROLE_TOURIST) {
            log.warn("Mobile login attempt blocked for non-tourist user {}", user.getEmail());
            throw new UnauthorizedException("Only tourist accounts are allowed on the mobile app.");
        }

        // Perform authentication (Spring Security)
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail().toLowerCase(), request.getPassword())
            );
        } catch (AuthenticationException ex) {
            log.error("Authentication failed for {}: {}", request.getEmail(), ex.getMessage());
            throw new BadCredentialsException("Invalid email or password.");
        }

        if (user.getStatus() == UserStatus.PENDING) {
            log.warn("Login failed: User account is pending approval for {}", request.getEmail());
            throw new UserPendingApprovalException("Your account is pending approval. Please wait for admin approval.");
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Generate tokens
        String jwt = jwtUtils.generateToken(userDetails);
        String refreshToken = jwtUtils.generateRefreshToken(userDetails);

        refreshTokenRedisService.storeToken(userDetails.getEmail(), refreshToken);

        log.info("User {} logged in successfully with role: {}", userDetails.getUsername(), userDetails.getAuthorities());

        // Build and return the login response
        return APIResponse.<LoginResponse>builder()
                .success(true)
                .message("User logged in successfully.")
                .data(LoginResponse.builder()
                        .id(userDetails.getId())
                        .email(userDetails.getUsername())
                        .role(UserRole.valueOf(userDetails.getAuthorities().stream().findFirst().orElseThrow().getAuthority()))
                        .jwtToken(jwt)
                        .refreshToken(refreshToken)
                        .emailVerified(userDetails.isEmailVerified())
                        .build())
                .build();
    }

    @Override
    public APIResponse<String> logoutUser(HttpServletRequest request) {
        log.info("Attempting logout for request: {}", request.getRequestURI());

        String email = authUtills.loggedInEmail();
        if (email == null) {
            log.warn("Logout failed: Email not found in JWT token.");
            throw new UnauthorizedException("Email not found in JWT token.");
        }

        // Delete refresh token from Redis
        refreshTokenRedisService.deleteToken(email);
        SecurityContextHolder.clearContext();
        log.info("Refresh token deleted for email: {}", email);

        log.info("User {} logged out successfully.", email);
        return APIResponse.<String>builder()
                .success(true)
                .message("User logged out successfully.")
                .build();
    }

    @Override
    @Transactional
    public APIResponse<UserProfileDto> getLoggedUserProfile(HttpServletRequest request) {
        log.info("Fetching logged user profile for request: {}", request.getRequestURI());

        String jwtToken = jwtUtils.getJwtToken(request);
        String email = jwtUtils.getUserNameFromJwtToken(jwtToken);
        if (email == null) {
            log.warn("Failed to fetch user profile: Email not found in JWT token.");
            throw new UnauthorizedException("Email not found in JWT token.");
        }

        User user = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        switch (user.getRole()) {
            case ROLE_TOURIST -> {
                Tourist tourist = touristRepository.findByUserId(user.getUserId())
                        .orElseThrow(() -> new UserNotFoundException("Tourist profile not found."));

                TouristProfileDto touristProfile = TouristProfileDto.builder()
                        .id(user.getUserId())
                        .email(user.getEmail())
                        .role(user.getRole())
                        .status(user.getStatus())
                        .profilePictureUrl(user.getProfilePictureUrl())
                        .emailVerified(user.getEmailVerified())
                        .firstName(tourist.getFirstName())
                        .lastName(tourist.getLastName())
                        .phoneNumber(tourist.getPhoneNumber())
                        .country(tourist.getCountry())
                        .build();

                return APIResponse.<UserProfileDto>builder()
                        .success(true)
                        .message("Tourist profile fetched successfully.")
                        .data(touristProfile)
                        .build();
            }

            case ROLE_PROVIDER -> {
                Provider provider = providerRepository.findByUserId(user.getUserId())
                        .orElseThrow(() -> new UserNotFoundException("Provider profile not found."));

                ProviderProfileDto providerProfile = ProviderProfileDto.builder()
                        .id(user.getUserId())
                        .email(user.getEmail())
                        .role(user.getRole())
                        .status(user.getStatus())
                        .profilePictureUrl(user.getProfilePictureUrl())
                        .emailVerified(user.getEmailVerified())
                        .businessName(provider.getBusinessName())
                        .businessDescription(provider.getBusinessDescription())
                        .coverImageUrl(provider.getCoverImageUrl())
                        .location(modelMapper.map(provider.getLocation(), LocationDTO.class))
                        .accommodationApprovalStatus(provider.getAccommodationApprovalStatus())
                        .tourGuideApprovalStatus(provider.getTourGuideApprovalStatus())
                        .transportApprovalStatus(provider.getTransportApprovalStatus())
                        .activityApprovalStatus(provider.getActivityApprovalStatus())
                        .foodApprovalStatus(provider.getFoodApprovalStatus())
                        .build();

                return APIResponse.<UserProfileDto>builder()
                        .success(true)
                        .message("Provider profile fetched successfully.")
                        .data(providerProfile)
                        .build();
            }

            case ROLE_ADMIN -> {
                Admin admin = adminRepository.findByUserId(user.getUserId())
                        .orElseThrow(() -> new UserNotFoundException("Admin profile not found."));

                AdminProfileDto adminProfile = AdminProfileDto.builder()
                        .id(user.getUserId())
                        .email(user.getEmail())
                        .role(user.getRole())
                        .status(user.getStatus())
                        .profilePictureUrl(user.getProfilePictureUrl())
                        .firstName(admin.getFirstName())
                        .lastName(admin.getLastName())
                        .emailVerified(user.getEmailVerified())
                        .build();

                return APIResponse.<UserProfileDto>builder()
                        .success(true)
                        .message("Admin profile fetched successfully.")
                        .data(adminProfile)
                        .build();
            }

            default -> {
                log.error("Unsupported user role: {}", user.getRole());
                throw new UnsupportedOperationException("User role not supported: " + user.getRole());
            }
        }
    }

    @Override
    public APIResponse<LoginResponse> refreshToken(HttpServletRequest request) {
        log.info("Attempting to refresh token for request: {}", request.getRequestURI());

        String refreshToken = jwtUtils.getRefreshToken(request);
        String email = jwtUtils.getUserNameFromJwtToken(refreshToken);
        if (email == null) {
            log.warn("Email not found in refresh token.");
            throw new UnauthorizedException("Email not found in refresh token.");
        }

        // Validate and refresh the token
        if (!refreshTokenRedisService.validateRefreshToken(email, refreshToken)) {
            log.warn("Invalid or expired refresh token for email: {}", email);
            throw new UnauthorizedException("Invalid or expired refresh token.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        UserDetailsImpl userDetails = UserDetailsImpl.build(user);
        String newJwt = jwtUtils.generateToken(userDetails);
        String newRefreshToken = jwtUtils.generateRefreshToken(userDetails);

        // Update the Redis store with the new refresh token
        refreshTokenRedisService.storeToken(email, newRefreshToken);

        log.info("Tokens refreshed successfully for user: {}", email);

        return APIResponse.<LoginResponse>builder()
                .success(true)
                .message("Tokens refreshed successfully.")
                .data(LoginResponse.builder()
                        .id(user.getUserId())
                        .email(user.getEmail())
                        .role(user.getRole())
                        .jwtToken(newJwt)
                        .refreshToken(newRefreshToken)
                        .emailVerified(user.getEmailVerified())
                        .build())
                .build();
    }

    @Override
    public APIResponse<String> approveProvider(Long providerId) {
        log.info("Approving provider with ID: {}", providerId);

        User provider = userRepository.findById(providerId)
                .orElseThrow(() -> new UserNotFoundException("Provider not found with ID: " + providerId));

        if (provider.getRole() != UserRole.ROLE_PROVIDER) {
            log.error("Approval failed: User with ID {} is not a provider.", providerId);
            throw new BadRequestException("User is not a provider.", "UserID", providerId);
        }

        if (provider.getStatus() == UserStatus.ACTIVE) {
            log.warn("Provider with ID {} is already approved.", providerId);
            return APIResponse.<String>builder()
                    .success(true)
                    .message("Provider with ID " + providerId + " is already approved.")
                    .build();
        }

        provider.setStatus(UserStatus.ACTIVE);
        userRepository.save(provider);

        log.info("Provider with ID {} approved successfully.", providerId);
        return APIResponse.<String>builder()
                .success(true)
                .message("Provider with ID " + providerId + " approved successfully.")
                .build();
    }

    @Override
    public APIResponse<String> verifyEmail(String token) {
        log.info("Verifying email with token: {}", token);

        if (token == null || token.isEmpty()) {
            log.error("Email verification failed: Token is null or empty.");
            throw new BadRequestException("Email verification token is required.", "token", null);
        }

        String email = jwtUtils.getUserNameFromJwtToken(token);
        if (email == null) {
            log.error("Email verification failed: Invalid token.");
            throw new UnauthorizedException("Invalid email verification token.");
        }

        User user = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        if (user.getEmailVerified()) {
            log.warn("Email for {} is already verified.", email);
            return APIResponse.<String>builder()
                    .success(true)
                    .message("Email is already verified.")
                    .build();
        }

        user.setEmailVerified(true);
        userRepository.save(user);

        log.info("Email for {} verified successfully.", email);
        return APIResponse.<String>builder()
                .success(true)
                .message("Email verified successfully.")
                .build();
    }

    @Override
    public APIResponse<String> changePassword(ChangePaswordRequest changePasswordRequest) {
        User user = userRepository.findByEmail(authUtills.loggedInEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + authUtills.loggedInEmail()));
        log.info("Changing password for user: {}", user.getEmail());

        if (changePasswordRequest.getNewPassword() == null || changePasswordRequest.getNewPassword().isEmpty()) {
            log.error("Change password failed: New password is required.");
            throw new BadRequestException("New password is required.", "newPassword", null);
        }

        if (!passwordEncoder.matches(changePasswordRequest.getOldPassword(), user.getPassword())) {
            log.error("Change password failed: Current password is incorrect for user {}", user.getEmail());
            throw new BadCredentialsException("Current password is incorrect.");
        }

        user.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
        userRepository.save(user);
        log.info("Password changed successfully for user: {}", user.getEmail());
        return APIResponse.<String>builder()
                .success(true)
                .message("Password changed successfully.")
                .data("Password changed successfully.")
                .build();
    }

}