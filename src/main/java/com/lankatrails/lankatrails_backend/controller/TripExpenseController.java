package com.lankatrails.lankatrails_backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lankatrails.lankatrails_backend.dtos.ExpenseDTO;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.service.TripExpenseService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

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

    // @PostMapping("/expense/update")
    // public ResponseEntity<APIResponse<String>> updateExpense(@Valid @RequestBody ExpenseDTO expenseDTO) {
    //     log.info("Updating expense: {}", expenseDTO);
    //     APIResponse<String> response = tripExpenseService.updateExpense(expenseDTO);
    //     log.info("Expense updated successfully: {}", response.getMessage());
    //     return ResponseEntity.status(HttpStatus.OK)
    //             .body(response);
    // }

    // @PostMapping("/expense/delete")
    // public ResponseEntity<APIResponse<String>> deleteExpense(@Valid @RequestBody ExpenseDTO expenseDTO) {
    //     log.info("Deleting expense: {}", expenseDTO);
    //     APIResponse<String> response = tripExpenseService.deleteExpense(expenseDTO);
    //     log.info("Expense deleted successfully: {}", response.getMessage());
    //     return ResponseEntity.status(HttpStatus.OK)
    //             .body(response);
    // }
}
