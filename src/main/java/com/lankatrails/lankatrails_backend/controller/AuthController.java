package com.lankatrails.lankatrails_backend.controller;


import com.lankatrails.lankatrails_backend.dtos.request.ProviderRegistrationRequest;
import com.lankatrails.lankatrails_backend.dtos.request.TouristRegistrationRequest;
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
public class AuthController {

    private final RegistrationService registrationService;

    @PostMapping("/signup/tourist")
    public ResponseEntity<RegistrationResponse> registerTourist(
            @Valid @RequestBody TouristRegistrationRequest request) {
        RegistrationResponse tourist = registrationService.registerTourist(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tourist);
    }

    @PostMapping("/signup/provider")
    public ResponseEntity<RegistrationResponse> registerProvider(
            @Valid @RequestBody ProviderRegistrationRequest request) {
        RegistrationResponse provider = registrationService.registerProvider(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(provider);
    }
}