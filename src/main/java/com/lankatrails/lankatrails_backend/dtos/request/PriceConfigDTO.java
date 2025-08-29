package com.lankatrails.lankatrails_backend.dtos.request;

import com.lankatrails.lankatrails_backend.model.enums.PriceType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceConfigDTO {
    private Double fixedPrice;
    private Double pricePerUnit;
    private Double pricePerAdult;
    private Double pricePerChild;
    private PriceType priceType; // Use String to represent enum
    private Double extraChargePerUnit;
    private Double extraPerAdult;
    private Double extraPerChild;
    private PriceType extraChargeType; // Use String to represent enum
    private Boolean allowAdvancePayment;
    private Double advancePaymentPercentage;
    private Double advancePaymentFixedAmount;
    private Boolean requiresDeposit;
    private Double depositAmount;
}
