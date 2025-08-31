package com.lankatrails.lankatrails_backend.dtos.request;

import java.math.BigDecimal;

import com.lankatrails.lankatrails_backend.model.enums.PriceType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceConfigDTO {
    private BigDecimal fixedPrice;
    private BigDecimal pricePerUnit;
    private BigDecimal pricePerAdult;
    private BigDecimal pricePerChild;
    private PriceType priceType; // Use String to represent enum
    private BigDecimal extraChargePerUnit;
    private BigDecimal extraPerAdult;
    private BigDecimal extraPerChild;
    private PriceType extraChargeType; // Use String to represent enum
    private Boolean allowAdvancePayment;
    private BigDecimal advancePaymentPercentage;
    private BigDecimal advancePaymentFixedAmount;
    private Boolean requiresDeposit;
    private BigDecimal depositAmount;
}
