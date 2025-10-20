package com.lankatrails.lankatrails_backend.service;

import com.lankatrails.lankatrails_backend.dtos.request.ExternalBookingCreateRequest;
import com.lankatrails.lankatrails_backend.dtos.request.ExternalBookingUpdateRequest;
import com.lankatrails.lankatrails_backend.dtos.response.ExternalBookingResponse;
import com.lankatrails.lankatrails_backend.dtos.response.ExternalBookingStatsResponse;
import com.lankatrails.lankatrails_backend.model.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface ExternalBookingService {
    
    /**
     * Create a new external booking
     */
    ExternalBookingResponse createExternalBooking(ExternalBookingCreateRequest request);
    
    /**
     * Update an existing external booking
     */
    ExternalBookingResponse updateExternalBooking(Long bookingId, ExternalBookingUpdateRequest request);
    
    /**
     * Get external booking by ID
     */
    ExternalBookingResponse getExternalBookingById(Long bookingId);
    
    /**
     * Get all external bookings for a specific provider
     */
    List<ExternalBookingResponse> getExternalBookingsByProvider(Long providerId);
    
    /**
     * Get external bookings for a specific service
     */
    List<ExternalBookingResponse> getExternalBookingsByService(Long serviceId);
    
    /**
     * Get external bookings by status
     */
    List<ExternalBookingResponse> getExternalBookingsByStatus(BookingStatus status);
    
    /**
     * Get external bookings for a provider with pagination and status filter
     */
    Page<ExternalBookingResponse> getExternalBookingsByProviderAndStatus(Long providerId, 
                                                                        BookingStatus status, 
                                                                        Pageable pageable);
    
    /**
     * Get external bookings within a date range
     */
    List<ExternalBookingResponse> getExternalBookingsByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Cancel an external booking
     */
    ExternalBookingResponse cancelExternalBooking(Long bookingId);
    
    /**
     * Confirm an external booking
     */
    ExternalBookingResponse confirmExternalBooking(Long bookingId);
    
    /**
     * Check for booking conflicts for a service in a given time range
     */
    boolean hasBookingConflict(Long serviceId, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Get recent bookings for a provider (last 30 days)
     */
    List<ExternalBookingResponse> getRecentBookingsByProvider(Long providerId);
    
    /**
     * Delete an external booking
     */
    void deleteExternalBooking(Long bookingId);
    
    /**
     * Get booking statistics for a provider
     */
    ExternalBookingStatsResponse getBookingStatsByProvider(Long providerId);
}