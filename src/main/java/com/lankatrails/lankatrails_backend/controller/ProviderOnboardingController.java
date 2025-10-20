package com.lankatrails.lankatrails_backend.controller;

import com.lankatrails.lankatrails_backend.exception.BadRequestException;
import com.lankatrails.lankatrails_backend.model.Provider;
import com.lankatrails.lankatrails_backend.repositories.ProviderRepository;
import com.lankatrails.lankatrails_backend.security.utils.AuthUtils;
import com.stripe.model.Account;
import com.stripe.model.AccountLink;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/provider/onboarding")
@Slf4j
public class ProviderOnboardingController {

    @Autowired
    ProviderRepository providerRepository;

    @Autowired
    AuthUtils authUtils;

    @PostMapping("/create-account")
    public ResponseEntity<Map<String, String>> createProviderAccount() {
        Provider provider = providerRepository.findByUserId(authUtils.loggedInUserId())
                .orElseThrow(() -> new BadRequestException("Provider not found"));

        try {
            Map<String, Object> params = new HashMap<>();
            params.put("type", "express");
            params.put("country", "US");
            params.put("email", provider.getEmail());
            params.put("capabilities", Map.of(
                    "transfers", Map.of("requested", true),
                    "card_payments", Map.of("requested", true)
            ));

            Account account = Account.create(params);

            provider.setStripeAccountId(account.getId());
            providerRepository.save(provider);

            Map<String, Object> accountLinkParams = new HashMap<>();
            accountLinkParams.put("account", account.getId());
            accountLinkParams.put("refresh_url", "https://lankatrails.lk/onboarding/refresh");
            accountLinkParams.put("return_url", "https://lankatrails.lk/onboarding/success");
            accountLinkParams.put("type", "account_onboarding");

            AccountLink accountLink = AccountLink.create(accountLinkParams);

            return ResponseEntity.ok(Map.of("url", accountLink.getUrl()));
        } catch (Exception e) {
            log.error("Error creating Stripe account link: {}", e.getMessage());
            throw new BadRequestException("Failed to create Stripe account link: " + e.getMessage());
        }
    }

    @GetMapping("/account-status")
    public ResponseEntity<Map<String, Object>> getAccountStatus() {
        Provider provider = providerRepository.findByUserId(authUtils.loggedInUserId())
                .orElseThrow(() -> new BadRequestException("Provider not found"));

        if (provider.getStripeAccountId() == null) {
            throw new BadRequestException("Provider does not have a Stripe account");
        }

        try {
            Account account = Account.retrieve(provider.getStripeAccountId());
            Map<String, Object> response = new HashMap<>();
            response.put("account_id", account.getId());
            response.put("charges_enabled", account.getChargesEnabled());
            response.put("payouts_enabled", account.getPayoutsEnabled());
            response.put("capabilities", account.getCapabilities());
            response.put("details_submitted", account.getDetailsSubmitted());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving Stripe account: {}", e.getMessage());
            throw new BadRequestException("Failed to retrieve Stripe account: " + e.getMessage());
        }
    }
}
