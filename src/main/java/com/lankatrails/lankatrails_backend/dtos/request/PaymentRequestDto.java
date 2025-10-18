package com.lankatrails.lankatrails_backend.dtos.request;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequestDto {
    private Long bookingId;
    private String paymentIntentId;
    private String currency;
    private BigDecimal paymentAmount;
    private String clientSecret;
}
