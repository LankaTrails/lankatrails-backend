package com.lankatrails.lankatrails_backend.model;

import com.lankatrails.lankatrails_backend.dtos.PriceDTO;
import com.lankatrails.lankatrails_backend.model.enums.PriceType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Entity
@Getter
@Setter
@Table(name = "price_configurations")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceConfiguration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long priceConfigId;

    @OneToOne(mappedBy = "priceConfiguration", fetch = FetchType.LAZY)
    private Service service;

    @Column(name = "fixed_price", scale = 2)
    private BigDecimal fixedPrice;

    @Column(name = "price_per_unit", scale = 2)
    private BigDecimal pricePerUnit;

    @Column(name = "price_per_adult", scale = 2)
    private BigDecimal pricePerAdult;

    @Column(name = "price_per_child", scale = 2)
    private BigDecimal pricePerChild;

    @Column(name = "price_type")
    @Enumerated(EnumType.STRING)
    private PriceType priceType;

    @Column(name = "extra_charge_per_unit", scale = 2)
    private BigDecimal extraChargePerUnit;

    @Column(name = "extra_per_adult", scale = 2)
    private BigDecimal extraPerAdult;

    @Column(name = "extra_per_child", scale = 2)
    private BigDecimal extraPerChild;

    @Column(name = "extra_charge_type")
    @Enumerated(EnumType.STRING)
    private PriceType extraChargeType;

    @Column(name = "allow_advance_payment")
    private Boolean allowAdvancePayment;

    @Column(name = "advance_payment_percentage", precision = 2, scale = 2)
    private BigDecimal advancePaymentPercentage;

    @Column(name = "advance_payment_fixed_amount", scale = 2)
    private BigDecimal advancePaymentFixedAmount;

    @Column(name = "requires_deposit", scale = 2)
    private Boolean requiresDeposit;

    @Column(name = "deposit_amount", scale = 2)
    private BigDecimal depositAmount;

    public List<PriceDTO> getPriceWithType() {
        return Stream.of(
                fixedPrice != null ? new PriceDTO(fixedPrice, PriceType.FIXED) : null,
                pricePerUnit != null ? new PriceDTO(pricePerUnit, PriceType.PER_UNIT) : null,
                pricePerAdult != null ? new PriceDTO(pricePerAdult, PriceType.PER_PERSON) : null,
                pricePerChild != null ? new PriceDTO(pricePerChild, PriceType.PER_PERSON) : null
        ).filter(Objects::nonNull).toList();
    }

    public List<PriceDTO> getExtraChargesWithType() {
        return Stream.of(
                extraChargePerUnit != null ? new PriceDTO(extraChargePerUnit, PriceType.PER_UNIT) : null,
                extraPerAdult != null ? new PriceDTO(extraPerAdult, PriceType.PER_PERSON) : null,
                extraPerChild != null ? new PriceDTO(extraPerChild, PriceType.PER_PERSON) : null
        ).filter(Objects::nonNull).toList();
    }

    public BigDecimal calculateTotalPrice(int units, int adults, int children) {
        BigDecimal totalPrice = BigDecimal.ZERO;
        if (fixedPrice != null) {
            totalPrice = fixedPrice;
        }
        if (pricePerUnit != null) {
            totalPrice = totalPrice.add(pricePerUnit.multiply(BigDecimal.valueOf(units)));
        }
        if (pricePerAdult != null) {
            totalPrice = totalPrice.add(pricePerAdult.multiply(BigDecimal.valueOf(adults)));
        }
        if (pricePerChild != null) {
            totalPrice = totalPrice.add(pricePerChild.multiply(BigDecimal.valueOf(children)));
        }
        return totalPrice;
    }

}
