package com.lankatrails.lankatrails_backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ExpenseShareDto {
    private Long expenseId;
    private Double amount;
    private TripParticipantDto participant;
}
