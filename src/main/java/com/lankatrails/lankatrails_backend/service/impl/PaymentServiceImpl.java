package com.lankatrails.lankatrails_backend.service.impl;

import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.exception.BadRequestException;
import com.lankatrails.lankatrails_backend.model.Booking;
import com.lankatrails.lankatrails_backend.model.Payment;
import com.lankatrails.lankatrails_backend.model.Provider;
import com.lankatrails.lankatrails_backend.model.enums.BookingStatus;
import com.lankatrails.lankatrails_backend.model.enums.PaymentStatus;
import com.lankatrails.lankatrails_backend.repositories.BookingRepository;
import com.lankatrails.lankatrails_backend.repositories.PaymentRepository;
import com.lankatrails.lankatrails_backend.repositories.ProviderRepository;
import com.lankatrails.lankatrails_backend.service.PaymentService;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.PaymentIntent;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@Transactional
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    BookingRepository bookingRepository;

    @Autowired
    PaymentRepository paymentRepository;

    @Autowired
    ProviderRepository providerRepository;

    @Autowired
    ModelMapper modelMapper;

    @Override
    @Transactional
    public PaymentIntent createPaymentIntent(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BadRequestException("Booking not found"));

        Provider provider = booking.getTripItem().getService().getProvider();
        if (provider == null) {
            throw new BadRequestException("Provider not found for the service");
        }

        BigDecimal totalAmount = booking.getTotalPrice();
        BigDecimal commission = calculateCommission(totalAmount);
        BigDecimal providerAmount = totalAmount.subtract(commission);

        long amountInCents = totalAmount.multiply(BigDecimal.valueOf(100)).longValue();
        long providerAmountInCents = providerAmount.multiply(BigDecimal.valueOf(100)).longValue();
        long commissionInCents = commission.multiply(BigDecimal.valueOf(100)).longValue();

        // Create PaymentIntent with Stripe API (pseudo-code)
        Map<String, Object> params = Map.of(
                "amount", amountInCents,
                "currency", "USD",
                "payment_method_types", List.of("card"),
                "application_fee_amount", commissionInCents,
                "transfer_data", Map.of("destination", provider.getStripeAccountId()),
                "metadata", Map.of("bookingId", bookingId.toString())
        );

        try {
            PaymentIntent paymentIntent = PaymentIntent.create(params);

            // Save payment details in your database
            Payment payment = Payment.builder()
                    .paymentIntentId(paymentIntent.getId())
                    .status(PaymentStatus.PENDING)
                    .currency("usd")
                    .amount(totalAmount)
                    .commission(commission)
                    .providerAmount(providerAmount)
                    .stripeFee(null)
                    .stripeChargeId(null)
                    .stripeTransferId(null)
                    .booking(booking)
                    .createdAt(LocalDate.now().atTime(LocalTime.now()))
                    .build();
            payment = paymentRepository.save(payment);

            return paymentIntent;
        } catch (StripeException e) {
            log.error("Error creating PaymentIntent", e);
            throw new RuntimeException("Failed to create payment intent" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public APIResponse<String> confirmPayment(String paymentIntentId) {
        try {
            Payment payment = paymentRepository.findByPaymentIntentId(paymentIntentId)
                    .orElseThrow(() -> new BadRequestException("Payment not found"));

            payment.setStatus(PaymentStatus.COMPLETED);

            // Retrieve PaymentIntent first
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

            // Get the latest charge ID from the PaymentIntent
            String latestChargeId = paymentIntent.getLatestCharge();
            if (latestChargeId != null) {
                // Retrieve charge with expanded balance transaction
                Map<String, Object> chargeParams = Map.of(
                        "expand", List.of("balance_transaction")
                );
                Charge charge = Charge.retrieve(latestChargeId, chargeParams, null);

                payment.setStripeChargeId(charge.getId());

                // Handle Stripe fee safely
                if (charge.getBalanceTransactionObject() != null) {
                    payment.setStripeFee(
                            BigDecimal.valueOf(charge.getBalanceTransactionObject().getFee())
                                    .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP)
                    );
                } else {
                    // If balance transaction is not available, retrieve it separately
                    try {
                        String balanceTransactionId = charge.getBalanceTransaction();
                        if (balanceTransactionId != null) {
                            com.stripe.model.BalanceTransaction balanceTransaction =
                                    com.stripe.model.BalanceTransaction.retrieve(balanceTransactionId);
                            payment.setStripeFee(
                                    BigDecimal.valueOf(balanceTransaction.getFee())
                                            .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP)
                            );
                        } else {
                            log.warn("No balance transaction ID found for charge: {}", charge.getId());
                            payment.setStripeFee(BigDecimal.ZERO);
                        }
                    } catch (StripeException ex) {
                        log.warn("Could not retrieve balance transaction: {}", ex.getMessage());
                        payment.setStripeFee(BigDecimal.ZERO); // Set to zero if can't retrieve
                    }
                }

                payment.setStripeTransferId(
                        paymentIntent.getTransferData() != null ? paymentIntent.getTransferData().getDestination() : null
                );
            } else {
                log.warn("No charges found for PaymentIntent: {}", paymentIntentId);
                payment.setStripeFee(BigDecimal.ZERO);
            }

            paymentRepository.save(payment);

            Booking booking = payment.getBooking();
            booking.setBookingStatus(BookingStatus.CONFIRMED);
            booking.setPaidAmount(booking.getPaidAmount().add(payment.getAmount()));
            booking.setUpdatedAt(LocalDateTime.now());
            bookingRepository.save(booking);

            log.info("Payment confirmed for booking: {}", booking.getBookingId());
            return new APIResponse<>(true, "Payment confirmed successfully", paymentIntentId);
        } catch (StripeException e) {
            log.error("Stripe API error confirming payment: {}", e.getMessage());
            throw new RuntimeException("Failed to confirm payment: " + e.getMessage());
        }
    }


    private BigDecimal calculateCommission(BigDecimal totalAmount) {
        // Implement your commission calculation logic
        // This could be a percentage or fixed amount
        return totalAmount.multiply(BigDecimal.valueOf(0.10)); // 10% commission
    }

    private BigDecimal calculateStripeFee(BigDecimal amount) {
        // Calculate Stripe fees (2.9% + fixed fee)
        BigDecimal percentageFee = amount.multiply(BigDecimal.valueOf(0.029));
        BigDecimal fixedFee = BigDecimal.valueOf(0.30); // $0.30
        return percentageFee.add(fixedFee);
    }
}
