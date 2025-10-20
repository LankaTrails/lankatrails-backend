package com.lankatrails.lankatrails_backend.dtos.response;

import com.lankatrails.lankatrails_backend.dtos.ExpenseShareDto;
import com.lankatrails.lankatrails_backend.dtos.TripParticipantDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ExpenseResponseDTO {
    private Long expenseId;
    private double amount; // For individual participant share amount
    private double totalExpenseAmount; // For total expense amount
    private String expenseName;
    private String budgetCategory;
    private Long tripId;
    private LocalDateTime expenseDateTime;
    private TripParticipantDto createdByParticipant;
    private Boolean isThroughApp;
    private List<ExpenseShareDto> shares;
}