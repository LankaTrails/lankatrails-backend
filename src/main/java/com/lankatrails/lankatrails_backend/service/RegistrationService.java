package com.lankatrails.lankatrails_backend.service;


import com.lankatrails.lankatrails_backend.dtos.request.ProviderRegistrationRequest;
import com.lankatrails.lankatrails_backend.dtos.request.RegistrationRequest;
import com.lankatrails.lankatrails_backend.dtos.request.TouristRegistrationRequest;
import com.lankatrails.lankatrails_backend.dtos.response.RegistrationResponse;

public interface RegistrationService {
//    RegistrationResponse registerUser(RegistrationRequest request);

    RegistrationResponse registerTourist(TouristRegistrationRequest request);

    RegistrationResponse registerProvider(ProviderRegistrationRequest request);
}