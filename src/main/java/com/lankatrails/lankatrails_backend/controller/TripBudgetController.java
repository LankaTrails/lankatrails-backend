package com.lankatrails.lankatrails_backend.controller;

import com.lankatrails.lankatrails_backend.dtos.request.TripBudgetLimitDto;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.service.TripBudgetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/trips-budget")
public class TripBudgetController {
    @Autowired
    private TripBudgetService tripBudgetService;

    @PostMapping("/category-limit")
    public ResponseEntity<APIResponse<TripBudgetLimitDto>> addTripBudgetLimit(@RequestBody TripBudgetLimitDto tripBudgetLimitDto) {
        log.info("Received request to add trip budget limit: {}", tripBudgetLimitDto);
        APIResponse<TripBudgetLimitDto> response = tripBudgetService.addTripBudgetLimit(tripBudgetLimitDto);
        log.info("Trip budget limit added successfully: {}", response.getData());
        return ResponseEntity.status(response.isSuccess() ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @PutMapping("/category-limit")
    public ResponseEntity<APIResponse<TripBudgetLimitDto>> updateTripBudgetLimit(@RequestBody TripBudgetLimitDto tripBudgetLimitDto) {
        log.info("Received request to update trip budget limit: {}", tripBudgetLimitDto);
        APIResponse<TripBudgetLimitDto> response = tripBudgetService.updateTripBudgetLimit(tripBudgetLimitDto);
        log.info("Trip budget limit updated successfully: {}", response.getData());
        return ResponseEntity.status(response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @GetMapping("/category-limit/{tripId}")
    public ResponseEntity<APIResponse<List<TripBudgetLimitDto>>> getTripBudgetLimitsByTripId(@PathVariable Long tripId) {
        log.info("Received request to get trip budget limits for trip ID: {}", tripId);
        APIResponse<List<TripBudgetLimitDto>> response = tripBudgetService.getTripBudgetLimitsByTripId(tripId);
        log.info("Trip budget limits retrieved successfully for trip ID {}: {}", tripId, response.getData());
        return ResponseEntity.status(response.isSuccess() ? HttpStatus.OK : HttpStatus.NOT_FOUND)
                .body(response);
    }
}
