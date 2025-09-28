package com.lankatrails.lankatrails_backend.dtos.request;

import com.lankatrails.lankatrails_backend.model.enums.PriceType;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceConfigDTO {
    private Long priceConfigId;
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
