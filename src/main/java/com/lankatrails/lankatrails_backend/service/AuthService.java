package com.lankatrails.lankatrails_backend.service;


import com.lankatrails.lankatrails_backend.dtos.request.*;
import com.lankatrails.lankatrails_backend.dtos.response.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AuthService {
//    RegistrationResponse registerUser(RegistrationRequest request);

    APIResponse<RegistrationResponse> registerTourist(TouristRegistrationRequest request);

    APIResponse<RegistrationResponse> registerProvider(ProviderRegistrationRequest request,
                                                       MultipartFile profilePicture,
                                                       MultipartFile coverPhoto,
                                                       MultipartFile businessRegistrationFile,
                                                       MultipartFile contactPersonIdentityFile,
                                                       List<MultipartFile> licenseFiles);

    APIResponse<LoginResponse> authenticateUser(LoginRequest request, HttpServletRequest httpServletRequest);

    APIResponse<String> logoutUser(HttpServletRequest request);

    APIResponse<UserProfileDto> getLoggedUserProfile(HttpServletRequest request);

    APIResponse<LoginResponse> refreshToken(HttpServletRequest request);

    APIResponse<String> approveProvider(Long providerId);

    APIResponse<String> verifyEmail(String token);

    APIResponse<String> changePassword(ChangePaswordRequest changePasswordRequest);

    APIResponse<ApproveLicenseResponse> approveProviderService();

    APIResponse<ProviderViewInfoResponse> loadAllRequestedProviders(Long id);

    APIResponse<ProviderInfoResponse> getBasicProviderInfo();

    APIResponse<String> approveOrRejectRequest(AcceptRejectDTO acceptRejectDTO);
}