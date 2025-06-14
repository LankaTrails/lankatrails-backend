package com.lankatrails.lankatrails_backend.controller;


import com.lankatrails.lankatrails_backend.dtos.request.RegistrationRequest;
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
@RequestMapping("/api/register")
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;

    @PostMapping
    public ResponseEntity<APIResponse<RegistrationResponse>> registerUser(
            @Valid @RequestBody RegistrationRequest request) {
        RegistrationResponse response = registrationService.registerUser(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new APIResponse<>(true, "Registration successful", response));
    }
}