package com.lankatrails.lankatrails_backend.service.impl;

import com.lankatrails.lankatrails_backend.config.StripeConfig;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.model.Booking;
import com.lankatrails.lankatrails_backend.model.Payment;
import com.lankatrails.lankatrails_backend.model.enums.BookingStatus;
import com.lankatrails.lankatrails_backend.model.enums.PaymentStatus;
import com.lankatrails.lankatrails_backend.repositories.BookingRepository;
import com.lankatrails.lankatrails_backend.repositories.PaymentRepository;
import com.lankatrails.lankatrails_backend.service.StripeService;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Transfer;
import com.stripe.net.Webhook;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class StripServiceImpl implements StripeService {
    @Autowired
    StripeConfig stripeConfig;

    @Autowired
    PaymentRepository paymentRepository;

    @Autowired
    BookingRepository bookingRepository;


    @Override
    public APIResponse<String> handleStripeWebhook(String payload, String sigHeader) {
        String endpointSecret = stripeConfig.getWebhookSecret();
        try {
            Event event = Webhook.constructEvent(payload, sigHeader, endpointSecret);

            switch (event.getType()) {
                case "payment_intent.succeeded":
                    handlePaymentIntentSucceeded(event);
                    break;
                case "payment_intent.payment_failed":
                    handlePaymentIntentFailed(event);
                    break;
                case "transfer.paid":
                    handleTransferPaid(event);
                    break;
                default:
                    log.info("Unhandled event type: {}", event.getType());
            }
            return APIResponse.<String>builder()
                    .success(true)
                    .message("Webhook handled successfully")
                    .data(null)
                    .build();
        } catch (Exception e) {
            log.error("Error handling Stripe webhook: {}", e.getMessage());
            return APIResponse.<String>builder()
                    .success(false)
                    .message("Webhook handling failed: " + e.getMessage())
                    .data(null)
                    .build();
        }
    }

    private void handlePaymentIntentSucceeded(Event event) {
        PaymentIntent paymentIntent = (PaymentIntent) event.getData().getObject();
        String paymentIntentId = paymentIntent.getId();

        // Update payment status
        Payment payment = paymentRepository.findByPaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setStripeChargeId(paymentIntent.getLatestCharge());
        paymentRepository.save(payment);

        // Update booking status
        Booking booking = payment.getBooking();
        booking.setBookingStatus(BookingStatus.CONFIRMED);
        booking.setPaidAmount(booking.getPaidAmount().add(payment.getAmount()));
        bookingRepository.save(booking);

        log.info("Payment succeeded for booking: {}", booking.getBookingId());
    }

    private void handlePaymentIntentFailed(Event event) {
        PaymentIntent paymentIntent = (PaymentIntent) event.getData().getObject();
        String paymentIntentId = paymentIntent.getId();

        // Update payment status
        Payment payment = paymentRepository.findByPaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        payment.setStatus(PaymentStatus.FAILED);
        paymentRepository.save(payment);

        // Update booking status
        Booking booking = payment.getBooking();
        booking.setBookingStatus(BookingStatus.PAYMENT_FAILED);
        bookingRepository.save(booking);

        log.warn("Payment failed for booking: {}", booking.getBookingId());
    }

    private void handleTransferPaid(Event event) {
        Transfer transfer = (Transfer) event.getData().getObject();
        String transferId = transfer.getId();

        // Update payment with transfer ID
        Payment payment = paymentRepository.findByStripeTransferId(transferId)
                .orElseGet(() -> {
                    // Try to find by metadata if not found directly
                    String paymentIntentId = transfer.getSourceTransaction();
                    return paymentRepository.findByPaymentIntentId(paymentIntentId)
                            .orElseThrow(() -> new RuntimeException("Payment not found for transfer"));
                });

        payment.setStripeTransferId(transferId);
        paymentRepository.save(payment);

        log.info("Transfer completed for payment: {}", payment.getPaymentId());
    }
}
