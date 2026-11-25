package com.lankatrails.lankatrails_backend.service;

import com.lankatrails.lankatrails_backend.dtos.request.ComplaintHandleRequestDTO;
import com.lankatrails.lankatrails_backend.dtos.request.ComplaintViewDTO;
import com.lankatrails.lankatrails_backend.exception.ConflictException;
import com.lankatrails.lankatrails_backend.exception.PaymentGatewayException;
import com.lankatrails.lankatrails_backend.model.Booking;
import com.lankatrails.lankatrails_backend.model.Complaint;
import com.lankatrails.lankatrails_backend.model.Payment;
import com.lankatrails.lankatrails_backend.model.enums.ComplaintResult;
import com.lankatrails.lankatrails_backend.model.enums.ComplaintStatus;
import com.lankatrails.lankatrails_backend.model.enums.PaymentStatus;
import com.lankatrails.lankatrails_backend.model.enums.RefundStatus;
import com.lankatrails.lankatrails_backend.repositories.ComplaintRepository;
import com.lankatrails.lankatrails_backend.repositories.PaymentRepository;
import com.lankatrails.lankatrails_backend.service.impl.ComplaintServiceImpl;
import com.lankatrails.lankatrails_backend.service.payment.ComplaintRefundService;
import com.lankatrails.lankatrails_backend.service.payment.StripeGateway;
import com.stripe.exception.ApiConnectionException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ComplaintRefundTest {

    static Complaint paidComplaint() {
        Payment payment = new Payment();
        payment.setPaymentIntentId("pi_123");
        payment.setStatus(PaymentStatus.COMPLETED);
        Booking booking = new Booking();
        booking.setBookingId(7L);
        booking.getPayments().add(payment);
        Complaint complaint = new Complaint();
        complaint.setComplaintId(42L);
        complaint.setComplaintStatus(ComplaintStatus.IN_PROGRESS);
        complaint.setBooking(booking);
        return complaint;
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    class RefundService {
        @Mock StripeGateway stripeGateway;
        @Mock PaymentRepository paymentRepository;
        @InjectMocks ComplaintRefundService refundService;

        @Test
        void providerFaultReversesTheProvidersTransfer() throws Exception {
            Complaint complaint = paidComplaint();
            when(stripeGateway.refundPaymentIntent(eq("pi_123"), eq(true), eq("complaint-refund-42"), anyMap())).thenReturn("re_1");

            assertThat(refundService.refund(complaint, ComplaintResult.REFUND_FROM_PROVIDER)).isEqualTo("re_1");
            assertThat(complaint.getRefundStatus()).isEqualTo(RefundStatus.REFUNDED);
            assertThat(complaint.getStripeRefundId()).isEqualTo("re_1");
            assertThat(complaint.getBooking().getPayments().iterator().next().getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        }

        @Test
        void companyRefundLeavesTheProvidersPayoutAlone() throws Exception {
            when(stripeGateway.refundPaymentIntent(eq("pi_123"), eq(false), anyString(), anyMap())).thenReturn("re_2");

            refundService.refund(paidComplaint(), ComplaintResult.REFUND_FROM_COMPANY);

            verify(stripeGateway).refundPaymentIntent(eq("pi_123"), eq(false), anyString(), anyMap());
        }

        @Test
        void complaintWithoutBookingCannotBeRefunded() throws Exception {
            Complaint complaint = paidComplaint();
            complaint.setBooking(null);

            assertThatThrownBy(() -> refundService.refund(complaint, ComplaintResult.REFUND_FROM_COMPANY))
                    .isInstanceOf(ConflictException.class);
            verify(stripeGateway, never()).refundPaymentIntent(anyString(), anyBoolean(), anyString(), anyMap());
        }

        @Test
        void alreadyRefundedComplaintIsNotRefundedTwice() throws Exception {
            Complaint complaint = paidComplaint();
            complaint.setRefundStatus(RefundStatus.REFUNDED);

            assertThatThrownBy(() -> refundService.refund(complaint, ComplaintResult.REFUND_FROM_PROVIDER))
                    .isInstanceOf(ConflictException.class);
            verify(stripeGateway, never()).refundPaymentIntent(anyString(), anyBoolean(), anyString(), anyMap());
        }

        @Test
        void rejectResultIsNotARefund() {
            assertThatThrownBy(() -> refundService.refund(paidComplaint(), ComplaintResult.REJECT))
                    .isInstanceOf(ConflictException.class);
        }

        @Test
        void stripeFailureSurfacesAsGatewayError() throws Exception {
            when(stripeGateway.refundPaymentIntent(anyString(), anyBoolean(), anyString(), anyMap()))
                    .thenThrow(new ApiConnectionException("network down"));

            Complaint complaint = paidComplaint();
            assertThatThrownBy(() -> refundService.refund(complaint, ComplaintResult.REFUND_FROM_PROVIDER))
                    .isInstanceOf(PaymentGatewayException.class);
            assertThat(complaint.getRefundStatus()).isNull();
        }
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    class ComplaintHandling {
        @Mock ComplaintRepository complaintRepository;
        @Mock ComplaintRefundService complaintRefundService;
        @InjectMocks ComplaintServiceImpl complaintService;

        @Test
        void handleComplaintRefundsAndResolves() {
            Complaint complaint = paidComplaint();
            when(complaintRepository.findById(42L)).thenReturn(Optional.of(complaint));

            ComplaintHandleRequestDTO request = new ComplaintHandleRequestDTO();
            request.setComplaintId(42L);
            request.setComplaintResult(ComplaintResult.REFUND_FROM_PROVIDER);

            var response = complaintService.handleComplaint(request);

            assertThat(response).isNotNull();
            assertThat(response.isSuccess()).isTrue();
            assertThat(complaint.getComplaintStatus()).isEqualTo(ComplaintStatus.RESOLVED);
            assertThat(complaint.getComplaintResult()).isEqualTo(ComplaintResult.REFUND_FROM_PROVIDER);
            verify(complaintRefundService).refund(complaint, ComplaintResult.REFUND_FROM_PROVIDER);
            verify(complaintRepository).save(complaint);
        }

        @Test
        void rejectingDoesNotRefund() {
            Complaint complaint = paidComplaint();
            when(complaintRepository.findById(42L)).thenReturn(Optional.of(complaint));

            ComplaintHandleRequestDTO request = new ComplaintHandleRequestDTO();
            request.setComplaintId(42L);
            request.setComplaintResult(ComplaintResult.REJECT);

            complaintService.handleComplaint(request);

            assertThat(complaint.getComplaintStatus()).isEqualTo(ComplaintStatus.RESOLVED);
            verify(complaintRefundService, never()).refund(any(), any());
        }

        @Test
        void resolvedComplaintCannotBeHandledAgain() {
            Complaint complaint = paidComplaint();
            complaint.setComplaintStatus(ComplaintStatus.RESOLVED);
            when(complaintRepository.findById(42L)).thenReturn(Optional.of(complaint));

            ComplaintHandleRequestDTO request = new ComplaintHandleRequestDTO();
            request.setComplaintId(42L);
            request.setComplaintResult(ComplaintResult.REFUND_FROM_COMPANY);

            assertThatThrownBy(() -> complaintService.handleComplaint(request)).isInstanceOf(ConflictException.class);
            verify(complaintRefundService, never()).refund(any(), any());
        }

        @Test
        void adminRefundEndpointIssuesTheRealRefund() {
            Complaint complaint = paidComplaint();
            complaint.setComplaintResult(ComplaintResult.REFUND_FROM_COMPANY);
            when(complaintRepository.findById(42L)).thenReturn(Optional.of(complaint));

            ComplaintViewDTO dto = new ComplaintViewDTO();
            dto.setRefundStatus(RefundStatus.REFUNDED);
            dto.setRefundReason("Room was not as described");

            complaintService.updateRefundStatus(42L, dto);

            verify(complaintRefundService).refund(complaint, ComplaintResult.REFUND_FROM_COMPANY);
            assertThat(complaint.getRefundReason()).isEqualTo("Room was not as described");
            assertThat(complaint.getComplaintStatus()).isEqualTo(ComplaintStatus.RESOLVED);
        }
    }
}
