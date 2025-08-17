package com.lankatrails.lankatrails_backend.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ExpenseResponseDTO {
    private Long expenseId;
    private double amount;
    private String expenseName;
    private String budgetCategory;
    private Long tripId;
}