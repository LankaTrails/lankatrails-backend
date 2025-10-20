package com.lankatrails.lankatrails_backend.service;

import com.lankatrails.lankatrails_backend.dtos.AvailabilityDto;
import com.lankatrails.lankatrails_backend.dtos.BookingItemDto;
import com.lankatrails.lankatrails_backend.dtos.request.PaymentRequestDto;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.AvailabilityResponse;
import com.lankatrails.lankatrails_backend.dtos.response.BookingResponseDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface BookingService {
    APIResponse<AvailabilityResponse> checkAvailability(AvailabilityDto availabilityDto);

    APIResponse<PaymentRequestDto> addNewBooking(Long tripItemId);
  
    APIResponse<String> cancelItem(Long tripItemId);
  
    APIResponse<BookingResponseDTO> getBookingsOnTheDay(AvailabilityDto availabilityDto, Long id);

    APIResponse<List<BookingItemDto>> getAllBookingForTrip(Long tripId);

    APIResponse<List<BookingItemDto>> getBookings(Long serviceId, LocalDateTime from, LocalDateTime to);

    Long countBookingsForServiceInPeriod(Long serviceId, LocalDateTime from, LocalDateTime to);

    Long countFutureBookingsForService(Long serviceId, LocalDateTime from);

    Long countPastBookingsForService(Long serviceId, LocalDateTime to);

    // Admin-specific methods
    APIResponse<List<BookingItemDto>> getAllBookings();

    APIResponse<List<BookingItemDto>> getBookingsByStatus(String status);

    APIResponse<List<BookingItemDto>> getBookingsByDateRange(LocalDateTime from, LocalDateTime to);

    APIResponse<Map<String, Object>> getBookingStatistics();

    APIResponse<List<BookingItemDto>> getRecentBookings(Integer limit);

    // Advanced Analytics Methods
    APIResponse<Map<String, Object>> getTouristAnalytics();

    APIResponse<Map<String, Object>> getBookingAnalytics(LocalDateTime from, LocalDateTime to);

    APIResponse<Map<String, Object>> getRevenueAnalytics(LocalDateTime from, LocalDateTime to);

    APIResponse<Map<String, Object>> getServiceProviderAnalytics();

    APIResponse<Map<String, Object>> getMonthlyAnalytics(int year);

    APIResponse<Map<String, Object>> getTopServicesAnalytics(Integer limit);

    APIResponse<Map<String, Object>> getDashboardAnalytics();
}
