package com.lankatrails.lankatrails_backend.dtos;

import com.lankatrails.lankatrails_backend.model.enums.BudgetCategory;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TripBudgetCategoryDto {
    private Long limitId;
    private BudgetCategory budgetCategory;
    private BigDecimal limitAmount;
    private BigDecimal spentAmount = BigDecimal.valueOf(0.0);
    private Long tripId;
}
