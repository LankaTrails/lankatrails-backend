package com.lankatrails.lankatrails_backend.controller;

import com.lankatrails.lankatrails_backend.dtos.request.TripBudgetLimitDto;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.service.TripBudgetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/trips-budget")
public class TripBudgetController {
    @Autowired
    private TripBudgetService tripBudgetService;

    @PostMapping("/limit")
    public ResponseEntity<APIResponse<TripBudgetLimitDto>> addTripBudgetLimit(@RequestBody TripBudgetLimitDto tripBudgetLimitDto) {
        log.info("Received request to add trip budget limit: {}", tripBudgetLimitDto);
        APIResponse<TripBudgetLimitDto> response = tripBudgetService.addTripBudgetLimit(tripBudgetLimitDto);
        log.info("Trip budget limit added successfully: {}", response.getData());
        return ResponseEntity.status(response.isSuccess() ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST)
                .body(response);
    }
}
