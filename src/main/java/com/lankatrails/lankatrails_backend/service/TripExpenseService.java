package com.lankatrails.lankatrails_backend.service;

import java.util.List;

import com.lankatrails.lankatrails_backend.dtos.ExpenseDTO;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.ExpenseResponseDTO;

public interface TripExpenseService {

    APIResponse<String> createExpense(ExpenseDTO expenseDTO);

    APIResponse<List<ExpenseResponseDTO>> getExpensesByTripId(Long tripId);
}
