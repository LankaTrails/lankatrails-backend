package com.lankatrails.lankatrails_backend.service.payment;

import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import com.stripe.model.AccountLink;
import com.stripe.model.Refund;
import com.stripe.net.RequestOptions;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Thin wrapper around the static Stripe SDK calls, so business logic can be unit-tested
 * with a mock instead of hitting the Stripe API.
 */
@Component
public class StripeGateway {

    /**
     * Fully refunds a PaymentIntent.
     *
     * @param reverseTransfer for destination charges: {@code true} pulls the provider's share back
     *                        from their connected account (provider bears the refund), {@code false}
     *                        leaves it with them (platform bears the refund)
     * @return the Stripe refund id
     */
    public String refundPaymentIntent(String paymentIntentId, boolean reverseTransfer,
                                      String idempotencyKey, Map<String, String> metadata) throws StripeException {
        Map<String, Object> params = new HashMap<>();
        params.put("payment_intent", paymentIntentId);
        params.put("reverse_transfer", reverseTransfer);
        params.put("reason", "requested_by_customer");
        params.put("metadata", metadata);
        return Refund.create(params, idempotent(idempotencyKey)).getId();
    }

    /** Creates a Stripe Connect Express account and returns its id. */
    public String createExpressAccount(String email, String country, String idempotencyKey) throws StripeException {
        Map<String, Object> params = new HashMap<>();
        params.put("type", "express");
        params.put("country", country);
        params.put("email", email);
        params.put("capabilities", Map.of(
                "transfers", Map.of("requested", true),
                "card_payments", Map.of("requested", true)
        ));
        return Account.create(params, idempotent(idempotencyKey)).getId();
    }

    /** Creates a single-use onboarding link for a connected account and returns its URL. */
    public String createOnboardingLink(String accountId, String refreshUrl, String returnUrl) throws StripeException {
        Map<String, Object> params = new HashMap<>();
        params.put("account", accountId);
        params.put("refresh_url", refreshUrl);
        params.put("return_url", returnUrl);
        params.put("type", "account_onboarding");
        return AccountLink.create(params).getUrl();
    }

    public Account retrieveAccount(String accountId) throws StripeException {
        return Account.retrieve(accountId);
    }

    private static RequestOptions idempotent(String key) {
        return RequestOptions.builder().setIdempotencyKey(key).build();
    }
}
