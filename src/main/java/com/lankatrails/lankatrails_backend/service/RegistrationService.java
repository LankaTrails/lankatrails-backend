package com.lankatrails.lankatrails_backend.service;


import com.lankatrails.lankatrails_backend.dtos.request.RegistrationRequest;
import com.lankatrails.lankatrails_backend.dtos.response.RegistrationResponse;

public interface RegistrationService {
    RegistrationResponse registerUser(RegistrationRequest request);
}