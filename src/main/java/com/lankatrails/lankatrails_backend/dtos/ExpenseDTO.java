package com.lankatrails.lankatrails_backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ExpenseDTO {
    private Long expenseId;
    private String expenseName;
    private Long tripId;
    private String budgetCategory;
    private List<ExpenseShareDto> shares;
    private String expenseDateTime;
    private TripParticipantDto createdByParticipant;
    private Double totalExpenseAmount;
}
