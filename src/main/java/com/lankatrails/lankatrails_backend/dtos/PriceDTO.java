package com.lankatrails.lankatrails_backend.dtos;

import com.lankatrails.lankatrails_backend.model.enums.PriceType;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PriceDTO {
    private BigDecimal amount;
    private PriceType priceType;
}
