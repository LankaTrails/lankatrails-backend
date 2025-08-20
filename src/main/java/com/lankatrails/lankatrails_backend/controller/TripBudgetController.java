package com.lankatrails.lankatrails_backend.controller;

import com.lankatrails.lankatrails_backend.dtos.TripBudgetCategoryDto;
import com.lankatrails.lankatrails_backend.dtos.TripBudgetDto;
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
    public ResponseEntity<APIResponse<TripBudgetCategoryDto>> addTripBudgetLimit(@RequestBody TripBudgetCategoryDto tripBudgetCategoryDto) {
        log.info("Received request to add trip budget limit: {}", tripBudgetCategoryDto);
        APIResponse<TripBudgetCategoryDto> response = tripBudgetService.addTripBudgetLimit(tripBudgetCategoryDto);
        log.info("Trip budget limit added successfully: {}", response.getData());
        return ResponseEntity.status(response.isSuccess() ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @PutMapping("/category-limit")
    public ResponseEntity<APIResponse<TripBudgetCategoryDto>> updateTripBudgetLimit(@RequestBody TripBudgetCategoryDto tripBudgetCategoryDto) {
        log.info("Received request to update trip budget limit: {}", tripBudgetCategoryDto);
        APIResponse<TripBudgetCategoryDto> response = tripBudgetService.updateTripBudgetLimit(tripBudgetCategoryDto);
        log.info("Trip budget limit updated successfully: {}", response.getData());
        return ResponseEntity.status(response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @GetMapping("/category-limit/{tripId}")
    public ResponseEntity<APIResponse<List<TripBudgetCategoryDto>>> getTripBudgetLimitsByTripId(@PathVariable Long tripId) {
        log.info("Received request to get trip budget limits for trip ID: {}", tripId);
        APIResponse<List<TripBudgetCategoryDto>> response = tripBudgetService.getTripBudgetLimitsByTripId(tripId);
        log.info("Trip budget limits retrieved successfully for trip ID {}: {}", tripId, response.getData());
        return ResponseEntity.status(response.isSuccess() ? HttpStatus.OK : HttpStatus.NOT_FOUND)
                .body(response);
    }

    @GetMapping("/{tripId}")
    public ResponseEntity<APIResponse<TripBudgetDto>> getTripBudgetDetails(@PathVariable Long tripId) {
        log.info("Received request to get trip budget details for trip ID: {}", tripId);
        APIResponse<TripBudgetDto> response = tripBudgetService.getTripBudgetDetails(tripId);
        log.info("Trip budget details retrieved successfully for trip ID {}: {}", tripId, response.getData());
        return ResponseEntity.status(response.isSuccess() ? HttpStatus.OK : HttpStatus.NOT_FOUND)
                .body(response);
    }

    @PutMapping("/total-budget")
    public ResponseEntity<APIResponse<TripBudgetDto>> updateTripTotalBudget(@RequestBody TripBudgetDto tripBudgetDto) {
        log.info("Received request to update trip total budget: {}", tripBudgetDto);
        APIResponse<TripBudgetDto> response = tripBudgetService.updateTripTotalBudget(tripBudgetDto);
        log.info("Trip total budget updated successfully: {}", response.getData());
        return ResponseEntity.status(response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST)
                .body(response);
    }
}
