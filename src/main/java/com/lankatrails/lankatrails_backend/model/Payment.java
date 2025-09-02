package com.lankatrails.lankatrails_backend.model;

import com.lankatrails.lankatrails_backend.model.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "payments")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    @Column(name = "amount", scale = 2)
    private BigDecimal amount;

    @Column(name = "commission", scale = 2)
    private BigDecimal commission;

    @Column(name = "provider_amount", scale = 2)
    private BigDecimal providerAmount;

    @Column(name = "stripe_fee", scale = 2)
    private BigDecimal stripeFee;

    @Column(name = "currency")
    private String currency;

    @Column(name = "payment_intent_id")
    private String paymentIntentId;

    @Column(name = "stripe_charge_id")
    private String stripeChargeId;

    @Column(name = "stripe_transfer_id")
    private String stripeTransferId;

    @ManyToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
