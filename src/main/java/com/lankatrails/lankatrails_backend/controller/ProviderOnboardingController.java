package com.lankatrails.lankatrails_backend.controller;

import com.lankatrails.lankatrails_backend.service.payment.ProviderOnboardingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/provider/onboarding")
@RequiredArgsConstructor
public class ProviderOnboardingController {

    private final ProviderOnboardingService onboardingService;

    /** Creates the provider's Stripe Connect account if needed and returns an onboarding link. */
    @PostMapping("/create-account")
    public ResponseEntity<Map<String, String>> createProviderAccount() {
        return ResponseEntity.ok(Map.of("url", onboardingService.createOnboardingLink()));
    }

    @GetMapping("/account-status")
    public ResponseEntity<Map<String, Object>> getAccountStatus() {
        return ResponseEntity.ok(onboardingService.getAccountStatus());
    }
}
