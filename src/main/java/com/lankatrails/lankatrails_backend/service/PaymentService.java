package com.lankatrails.lankatrails_backend.service;

import com.lankatrails.lankatrails_backend.model.Provider;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.stripe.model.PaymentIntent;

public interface PaymentService {
    PaymentIntent createPaymentIntent(Long bookingId);

    /** Throws 409 if the provider has not connected a Stripe account and so cannot be paid. */
    void assertProviderCanReceivePayments(Provider provider);
    APIResponse<String> confirmPayment(String paymentIntentId);
}
