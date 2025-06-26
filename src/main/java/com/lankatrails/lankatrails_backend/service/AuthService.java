package com.lankatrails.lankatrails_backend.service;


import com.lankatrails.lankatrails_backend.dtos.request.LoginRequest;
import com.lankatrails.lankatrails_backend.dtos.request.ProviderRegistrationRequest;
import com.lankatrails.lankatrails_backend.dtos.request.TouristRegistrationRequest;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.LoginResponse;
import com.lankatrails.lankatrails_backend.dtos.response.RegistrationResponse;
import com.lankatrails.lankatrails_backend.dtos.response.UserProfileDto;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {
//    RegistrationResponse registerUser(RegistrationRequest request);

    APIResponse<RegistrationResponse> registerTourist(TouristRegistrationRequest request);

    APIResponse<RegistrationResponse> registerProvider(ProviderRegistrationRequest request);

    APIResponse<LoginResponse> authenticateUser(LoginRequest request);

    APIResponse<String> logoutUser(HttpServletRequest request);

    APIResponse<UserProfileDto> getLoggedUserProfile(HttpServletRequest request);

    APIResponse<LoginResponse> refreshToken(HttpServletRequest request);
}