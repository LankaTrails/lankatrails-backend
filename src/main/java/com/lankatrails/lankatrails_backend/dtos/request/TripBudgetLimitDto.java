package com.lankatrails.lankatrails_backend.dtos.request;

import com.lankatrails.lankatrails_backend.model.enums.BudgetCategory;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TripBudgetLimitDto {
    private Long limitId;
    private BudgetCategory budgetCategory;
    private Double limitAmount;
    private Long tripId;
}
