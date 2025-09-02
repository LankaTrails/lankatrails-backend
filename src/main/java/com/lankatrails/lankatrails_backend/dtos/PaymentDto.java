package com.lankatrails.lankatrails_backend.dtos;

import com.lankatrails.lankatrails_backend.model.enums.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentDto {
    private Long paymentId;
    private String paymentIntentId;
    private String stripeChargeId;
    private String stripeTransferId;
    private PaymentStatus status;
    private String currency;
    private BigDecimal amount;
    private BigDecimal commission;
    private BigDecimal providerAmount;
    private BigDecimal stripeFee;
    private Long bookingId;
}
