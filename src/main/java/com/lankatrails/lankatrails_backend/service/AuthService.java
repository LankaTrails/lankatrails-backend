package com.lankatrails.lankatrails_backend.service;


import com.lankatrails.lankatrails_backend.dtos.request.LoginRequest;
import com.lankatrails.lankatrails_backend.dtos.request.ProviderRegistrationRequest;
import com.lankatrails.lankatrails_backend.dtos.request.TouristRegistrationRequest;
import com.lankatrails.lankatrails_backend.dtos.response.LoginResponse;
import com.lankatrails.lankatrails_backend.dtos.response.RegistrationResponse;

public interface AuthService {
//    RegistrationResponse registerUser(RegistrationRequest request);

    RegistrationResponse registerTourist(TouristRegistrationRequest request);

    RegistrationResponse registerProvider(ProviderRegistrationRequest request);

    LoginResponse authenticateUser(LoginRequest request);
}