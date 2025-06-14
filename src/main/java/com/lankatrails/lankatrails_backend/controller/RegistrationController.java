package com.lankatrails.lankatrails_backend.controller;


import com.lankatrails.lankatrails_backend.dtos.request.ProviderRegistrationRequest;
import com.lankatrails.lankatrails_backend.dtos.request.RegistrationRequest;
import com.lankatrails.lankatrails_backend.dtos.request.TouristRegistrationRequest;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.RegistrationResponse;
import com.lankatrails.lankatrails_backend.service.RegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;

    @PostMapping("/signup/tourist")
    public ResponseEntity<APIResponse<String>> registerTourist(
            @Valid @RequestBody TouristRegistrationRequest request) {
        RegistrationResponse tourist = registrationService.registerTourist(request);
        return ResponseEntity.ok(
                new APIResponse<>(true, "Tourist registered successfully", tourist.getEmail())
        );
    }

    @PostMapping("/signup/provider")
    public ResponseEntity<APIResponse<String>> registerProvider(
            @Valid @RequestBody ProviderRegistrationRequest request) {
        RegistrationResponse provider = registrationService.registerProvider(request);
        return ResponseEntity.ok(
                new APIResponse<>(true, "Provider registered successfully", provider.getEmail())
        );
    }
}