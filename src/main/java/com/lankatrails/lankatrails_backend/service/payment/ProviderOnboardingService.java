package com.lankatrails.lankatrails_backend.service.payment;

import com.lankatrails.lankatrails_backend.exception.ConflictException;
import com.lankatrails.lankatrails_backend.exception.PaymentGatewayException;
import com.lankatrails.lankatrails_backend.exception.ResourceNotFoundException;
import com.lankatrails.lankatrails_backend.model.Provider;
import com.lankatrails.lankatrails_backend.repositories.ProviderRepository;
import com.lankatrails.lankatrails_backend.security.utils.AuthUtils;
import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

/** Stripe Connect Express onboarding for the logged-in provider. */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProviderOnboardingService {

    private final StripeGateway stripeGateway;
    private final ProviderRepository providerRepository;
    private final AuthUtils authUtils;

    @Value("${stripe.connect.country:US}")
    private String connectCountry;

    /** Provider dashboard origin; Stripe sends the provider back here after onboarding. */
    @Value("${app.provider-dashboard-url:http://localhost:5174}")
    private String providerDashboardUrl;

    /**
     * Returns a fresh onboarding link. The Stripe account is created only once per provider:
     * later calls reuse it (e.g. to finish an interrupted onboarding), and the idempotency key
     * stops a double click from creating two accounts.
     */
    @Transactional
    public String createOnboardingLink() {
        Provider provider = currentProvider();
        try {
            String accountId = provider.getStripeAccountId();
            if (accountId == null || accountId.isBlank()) {
                accountId = stripeGateway.createExpressAccount(
                        provider.getEmail(), connectCountry, "connect-account-provider-" + provider.getUserId());
                provider.setStripeAccountId(accountId);
                providerRepository.save(provider);
                log.info("Created Stripe Connect account {} for provider {}", accountId, provider.getUserId());
            }
            return stripeGateway.createOnboardingLink(accountId,
                    dashboardUrl("/provider/onboarding/refresh"),
                    dashboardUrl("/provider/onboarding/return"));
        } catch (StripeException e) {
            log.error("Stripe onboarding failed for provider {}: {}", provider.getUserId(), e.getMessage());
            throw new PaymentGatewayException("Stripe onboarding failed: " + e.getMessage(),
                    "Could not start payment setup. Please try again.");
        }
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getAccountStatus() {
        Provider provider = currentProvider();
        if (provider.getStripeAccountId() == null) {
            throw new ConflictException("Provider " + provider.getUserId() + " has no Stripe account",
                    "Payments have not been set up yet");
        }
        try {
            Account account = stripeGateway.retrieveAccount(provider.getStripeAccountId());
            Map<String, Object> response = new HashMap<>();
            response.put("account_id", account.getId());
            response.put("charges_enabled", account.getChargesEnabled());
            response.put("payouts_enabled", account.getPayoutsEnabled());
            response.put("capabilities", account.getCapabilities());
            response.put("details_submitted", account.getDetailsSubmitted());
            return response;
        } catch (StripeException e) {
            throw new PaymentGatewayException("Stripe account lookup failed: " + e.getMessage(),
                    "Could not load payment status. Please try again.");
        }
    }

    private Provider currentProvider() {
        Long userId = authUtils.loggedInUserId();
        return providerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider", userId));
    }

    private String dashboardUrl(String path) {
        return UriComponentsBuilder.fromUriString(providerDashboardUrl).path(path).toUriString();
    }
}
