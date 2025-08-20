package com.lankatrails.lankatrails_backend.dtos;

import com.lankatrails.lankatrails_backend.model.enums.BudgetCategory;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TripBudgetCategoryDto {
    private Long limitId;
    private BudgetCategory budgetCategory;
    private Double limitAmount;
    private Double spentAmount = 0.0;
    private Long tripId;
}
