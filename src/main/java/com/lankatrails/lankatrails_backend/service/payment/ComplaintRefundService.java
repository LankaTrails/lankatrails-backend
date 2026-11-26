package com.lankatrails.lankatrails_backend.service.payment;

import com.lankatrails.lankatrails_backend.exception.ConflictException;
import com.lankatrails.lankatrails_backend.exception.ErrorCode;
import com.lankatrails.lankatrails_backend.exception.PaymentGatewayException;
import com.lankatrails.lankatrails_backend.model.Booking;
import com.lankatrails.lankatrails_backend.model.Complaint;
import com.lankatrails.lankatrails_backend.model.Payment;
import com.lankatrails.lankatrails_backend.model.enums.ComplaintResult;
import com.lankatrails.lankatrails_backend.model.enums.PaymentStatus;
import com.lankatrails.lankatrails_backend.model.enums.RefundStatus;
import com.lankatrails.lankatrails_backend.repositories.PaymentRepository;
import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Issues the Stripe refund that resolves a complaint.
 * <ul>
 *   <li>{@code REFUND_FROM_PROVIDER}: the transfer to the provider is reversed, so the provider bears the cost.</li>
 *   <li>{@code REFUND_FROM_COMPANY}: the platform refunds from its own balance; the provider keeps their payout.</li>
 * </ul>
 * Callers are transactional, so a failed Stripe call rolls back the complaint update.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ComplaintRefundService {

    private final StripeGateway stripeGateway;
    private final PaymentRepository paymentRepository;

    public String refund(Complaint complaint, ComplaintResult result) {
        if (result != ComplaintResult.REFUND_FROM_PROVIDER && result != ComplaintResult.REFUND_FROM_COMPANY) {
            throw new ConflictException(ErrorCode.REFUND_NOT_POSSIBLE,
                    "Complaint result " + result + " does not call for a refund",
                    "Set the complaint result to a refund before processing it");
        }
        if (complaint.getRefundStatus() == RefundStatus.REFUNDED) {
            throw new ConflictException(ErrorCode.REFUND_NOT_POSSIBLE,
                    "Complaint " + complaint.getComplaintId() + " is already refunded",
                    "This complaint has already been refunded");
        }

        Booking booking = complaint.getBooking();
        if (booking == null) {
            throw new ConflictException(ErrorCode.REFUND_NOT_POSSIBLE,
                    "Complaint " + complaint.getComplaintId() + " has no booking",
                    "This complaint is not linked to a paid booking, so there is nothing to refund");
        }

        Payment payment = booking.getPayments().stream()
                .filter(p -> p.getStatus() == PaymentStatus.COMPLETED && p.getPaymentIntentId() != null)
                .findFirst()
                .orElseThrow(() -> new ConflictException(ErrorCode.REFUND_NOT_POSSIBLE,
                        "Booking " + booking.getBookingId() + " has no completed payment",
                        "The booking for this complaint has no completed payment to refund"));

        boolean providerPays = result == ComplaintResult.REFUND_FROM_PROVIDER;
        try {
            String refundId = stripeGateway.refundPaymentIntent(
                    payment.getPaymentIntentId(),
                    providerPays,
                    // Retries of the same complaint never refund twice
                    "complaint-refund-" + complaint.getComplaintId(),
                    Map.of(
                            "complaintId", String.valueOf(complaint.getComplaintId()),
                            "bookingId", String.valueOf(booking.getBookingId()),
                            "result", result.name()
                    ));

            payment.setStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(payment);
            complaint.setRefundStatus(RefundStatus.REFUNDED);
            complaint.setStripeRefundId(refundId);

            log.info("Refunded complaint {} (booking {}, {}): {}",
                    complaint.getComplaintId(), booking.getBookingId(), result, refundId);
            return refundId;
        } catch (StripeException e) {
            log.error("Stripe refund failed for complaint {}: {}", complaint.getComplaintId(), e.getMessage());
            throw new PaymentGatewayException("Stripe refund failed: " + e.getMessage(),
                    "The refund could not be processed by the payment provider. Please try again.");
        }
    }
}
