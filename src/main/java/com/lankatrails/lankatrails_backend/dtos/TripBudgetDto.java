package com.lankatrails.lankatrails_backend.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class TripBudgetDto {
    private Long tripId;
    private Double totalBudgetLimit = 0.0;
    private Double totalSpentAmount = 0.0;
    private Set<TripBudgetCategoryDto> tripBudgetCategories;
}
