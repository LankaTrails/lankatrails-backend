package com.lankatrails.lankatrails_backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lankatrails.lankatrails_backend.dtos.ExpenseDTO;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.ExpenseResponseDTO;
import com.lankatrails.lankatrails_backend.service.TripExpenseService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/trips")
public class TripExpenseController {
    @Autowired
    private TripExpenseService tripExpenseService;

    @PostMapping("/expense/create")
    public ResponseEntity<APIResponse<String>> createExpense(@Valid @RequestBody ExpenseDTO expenseDTO) {
        log.info("Creating expense: {}", expenseDTO);
        APIResponse<String> response = tripExpenseService.createExpense(expenseDTO);
        log.info("Expense created successfully: {}", response.getMessage());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/expense/{expenseId}")
    public ResponseEntity<APIResponse<String>> updateExpense(
            @PathVariable Long expenseId, 
            @Valid @RequestBody ExpenseDTO expenseDTO) {
        log.info("Updating expense with ID: {} - {}", expenseId, expenseDTO);
        APIResponse<String> response = tripExpenseService.updateExpense(expenseId, expenseDTO);
        log.info("Expense updated successfully: {}", response.getMessage());
        return ResponseEntity.status(HttpStatus.OK)
                .body(response);
    }

    @DeleteMapping("/expense/{expenseId}")
    public ResponseEntity<APIResponse<String>> deleteExpense(@PathVariable Long expenseId) {
        log.info("Deleting expense with ID: {}", expenseId);
        APIResponse<String> response = tripExpenseService.deleteExpense(expenseId);
        log.info("Expense deleted successfully: {}", response.getMessage());
        return ResponseEntity.status(HttpStatus.OK)
                .body(response);
    }

    @GetMapping("/expenses/{tripId}")
    public ResponseEntity<APIResponse<List<ExpenseResponseDTO>>> getExpensesByTripId(@PathVariable Long tripId) {
        log.info("Retrieving expenses for trip ID: {}", tripId);
        APIResponse<List<ExpenseResponseDTO>> response = tripExpenseService.getExpensesByTripId(tripId);
        log.info("Expenses retrieved successfully for trip ID {}: {} expenses found", tripId, 
                response.getData() != null ? response.getData().size() : 0);
        return ResponseEntity.status(response.isSuccess() ? HttpStatus.OK : HttpStatus.NOT_FOUND)
                .body(response);
    }
}
