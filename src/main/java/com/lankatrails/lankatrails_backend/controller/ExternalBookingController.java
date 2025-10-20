package com.lankatrails.lankatrails_backend.controller;

import com.lankatrails.lankatrails_backend.dtos.request.ExternalBookingCreateRequest;
import com.lankatrails.lankatrails_backend.dtos.request.ExternalBookingUpdateRequest;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.ExternalBookingResponse;
import com.lankatrails.lankatrails_backend.dtos.response.ExternalBookingStatsResponse;
import com.lankatrails.lankatrails_backend.model.enums.BookingStatus;
import com.lankatrails.lankatrails_backend.service.ExternalBookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/external-bookings")
@RequiredArgsConstructor
@Slf4j
public class ExternalBookingController {

    private final ExternalBookingService externalBookingService;

    /**
     * Create a new external booking
     */
    @PostMapping
    public ResponseEntity<APIResponse<ExternalBookingResponse>> createExternalBooking(
            @Valid @RequestBody ExternalBookingCreateRequest request) {
        log.info("Creating external booking for service ID: {}", request.getServiceId());
        
        ExternalBookingResponse response = externalBookingService.createExternalBooking(request);
        
        APIResponse<ExternalBookingResponse> apiResponse = APIResponse.<ExternalBookingResponse>builder()
            .success(true)
            .message("External booking created successfully")
            .data(response)
            .build();
            
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    /**
     * Update an existing external booking
     */
    @PutMapping("/{bookingId}")
    public ResponseEntity<APIResponse<ExternalBookingResponse>> updateExternalBooking(
            @PathVariable Long bookingId,
            @Valid @RequestBody ExternalBookingUpdateRequest request) {
        log.info("Updating external booking with ID: {}", bookingId);
        
        ExternalBookingResponse response = externalBookingService.updateExternalBooking(bookingId, request);
        
        APIResponse<ExternalBookingResponse> apiResponse = APIResponse.<ExternalBookingResponse>builder()
            .success(true)
            .message("External booking updated successfully")
            .data(response)
            .build();
            
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Get external booking by ID
     */
    @GetMapping("/{bookingId}")
    public ResponseEntity<APIResponse<ExternalBookingResponse>> getExternalBookingById(
            @PathVariable Long bookingId) {
        log.info("Fetching external booking with ID: {}", bookingId);
        
        ExternalBookingResponse response = externalBookingService.getExternalBookingById(bookingId);
        
        APIResponse<ExternalBookingResponse> apiResponse = APIResponse.<ExternalBookingResponse>builder()
            .success(true)
            .message("External booking fetched successfully")
            .data(response)
            .build();
            
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Get all external bookings for a specific provider
     */
    @GetMapping("/provider/{providerId}")
    public ResponseEntity<APIResponse<List<ExternalBookingResponse>>> getExternalBookingsByProvider(
            @PathVariable Long providerId) {
        log.info("Fetching external bookings for provider ID: {}", providerId);
        
        List<ExternalBookingResponse> response = externalBookingService.getExternalBookingsByProvider(providerId);
        
        APIResponse<List<ExternalBookingResponse>> apiResponse = APIResponse.<List<ExternalBookingResponse>>builder()
            .success(true)
            .message("External bookings fetched successfully")
            .data(response)
            .build();
            
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Get external bookings for a specific service
     */
    @GetMapping("/service/{serviceId}")
    public ResponseEntity<APIResponse<List<ExternalBookingResponse>>> getExternalBookingsByService(
            @PathVariable Long serviceId) {
        log.info("Fetching external bookings for service ID: {}", serviceId);
        
        List<ExternalBookingResponse> response = externalBookingService.getExternalBookingsByService(serviceId);
        
        APIResponse<List<ExternalBookingResponse>> apiResponse = APIResponse.<List<ExternalBookingResponse>>builder()
            .success(true)
            .message("External bookings fetched successfully")
            .data(response)
            .build();
            
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Get external bookings by status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<APIResponse<List<ExternalBookingResponse>>> getExternalBookingsByStatus(
            @PathVariable BookingStatus status) {
        log.info("Fetching external bookings with status: {}", status);
        
        List<ExternalBookingResponse> response = externalBookingService.getExternalBookingsByStatus(status);
        
        APIResponse<List<ExternalBookingResponse>> apiResponse = APIResponse.<List<ExternalBookingResponse>>builder()
            .success(true)
            .message("External bookings fetched successfully")
            .data(response)
            .build();
            
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Get external bookings for a provider with pagination and status filter
     */
    @GetMapping("/provider/{providerId}/status/{status}")
    public ResponseEntity<APIResponse<Page<ExternalBookingResponse>>> getExternalBookingsByProviderAndStatus(
            @PathVariable Long providerId,
            @PathVariable BookingStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "bookedDateTime") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        log.info("Fetching external bookings for provider ID: {} with status: {}", providerId, status);
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ExternalBookingResponse> response = externalBookingService.getExternalBookingsByProviderAndStatus(
            providerId, status, pageable);
        
        APIResponse<Page<ExternalBookingResponse>> apiResponse = APIResponse.<Page<ExternalBookingResponse>>builder()
            .success(true)
            .message("External bookings fetched successfully")
            .data(response)
            .build();
            
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Get external bookings within a date range
     */
    @GetMapping("/date-range")
    public ResponseEntity<APIResponse<List<ExternalBookingResponse>>> getExternalBookingsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("Fetching external bookings between {} and {}", startDate, endDate);
        
        List<ExternalBookingResponse> response = externalBookingService.getExternalBookingsByDateRange(startDate, endDate);
        
        APIResponse<List<ExternalBookingResponse>> apiResponse = APIResponse.<List<ExternalBookingResponse>>builder()
            .success(true)
            .message("External bookings fetched successfully")
            .data(response)
            .build();
            
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Cancel an external booking
     */
    @PutMapping("/{bookingId}/cancel")
    public ResponseEntity<APIResponse<ExternalBookingResponse>> cancelExternalBooking(
            @PathVariable Long bookingId) {
        log.info("Cancelling external booking with ID: {}", bookingId);
        
        ExternalBookingResponse response = externalBookingService.cancelExternalBooking(bookingId);
        
        APIResponse<ExternalBookingResponse> apiResponse = APIResponse.<ExternalBookingResponse>builder()
            .success(true)
            .message("External booking cancelled successfully")
            .data(response)
            .build();
            
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Confirm an external booking
     */
    @PutMapping("/{bookingId}/confirm")
    public ResponseEntity<APIResponse<ExternalBookingResponse>> confirmExternalBooking(
            @PathVariable Long bookingId) {
        log.info("Confirming external booking with ID: {}", bookingId);
        
        ExternalBookingResponse response = externalBookingService.confirmExternalBooking(bookingId);
        
        APIResponse<ExternalBookingResponse> apiResponse = APIResponse.<ExternalBookingResponse>builder()
            .success(true)
            .message("External booking confirmed successfully")
            .data(response)
            .build();
            
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Check for booking conflicts
     */
    @GetMapping("/service/{serviceId}/conflicts")
    public ResponseEntity<APIResponse<Boolean>> checkBookingConflict(
            @PathVariable Long serviceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("Checking booking conflicts for service ID: {} between {} and {}", serviceId, startDate, endDate);
        
        boolean hasConflict = externalBookingService.hasBookingConflict(serviceId, startDate, endDate);
        
        APIResponse<Boolean> apiResponse = APIResponse.<Boolean>builder()
            .success(true)
            .message(hasConflict ? "Booking conflict exists" : "No booking conflict")
            .data(hasConflict)
            .build();
            
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Get recent bookings for a provider (last 30 days)
     */
    @GetMapping("/provider/{providerId}/recent")
    public ResponseEntity<APIResponse<List<ExternalBookingResponse>>> getRecentBookingsByProvider(
            @PathVariable Long providerId) {
        log.info("Fetching recent bookings for provider ID: {}", providerId);
        
        List<ExternalBookingResponse> response = externalBookingService.getRecentBookingsByProvider(providerId);
        
        APIResponse<List<ExternalBookingResponse>> apiResponse = APIResponse.<List<ExternalBookingResponse>>builder()
            .success(true)
            .message("Recent external bookings fetched successfully")
            .data(response)
            .build();
            
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Delete an external booking
     */
    @DeleteMapping("/{bookingId}")
    public ResponseEntity<APIResponse<String>> deleteExternalBooking(@PathVariable Long bookingId) {
        log.info("Deleting external booking with ID: {}", bookingId);
        
        externalBookingService.deleteExternalBooking(bookingId);
        
        APIResponse<String> apiResponse = APIResponse.<String>builder()
            .success(true)
            .message("External booking deleted successfully")
            .data("Booking with ID " + bookingId + " has been deleted")
            .build();
            
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Get booking statistics for a provider
     */
    @GetMapping("/provider/{providerId}/stats")
    public ResponseEntity<APIResponse<ExternalBookingStatsResponse>> getBookingStatsByProvider(
            @PathVariable Long providerId) {
        log.info("Fetching booking statistics for provider ID: {}", providerId);
        
        ExternalBookingStatsResponse response = externalBookingService.getBookingStatsByProvider(providerId);
        
        APIResponse<ExternalBookingStatsResponse> apiResponse = APIResponse.<ExternalBookingStatsResponse>builder()
            .success(true)
            .message("Booking statistics fetched successfully")
            .data(response)
            .build();
            
        return ResponseEntity.ok(apiResponse);
    }
}