package com.lankatrails.lankatrails_backend.service;

import com.lankatrails.lankatrails_backend.dtos.AvailabilityDto;
import com.lankatrails.lankatrails_backend.dtos.BookingItemDto;
import com.lankatrails.lankatrails_backend.dtos.request.PaymentRequestDto;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.AvailabilityResponse;
import com.lankatrails.lankatrails_backend.dtos.response.BookingResponseDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingService {
    APIResponse<AvailabilityResponse> checkAvailability(AvailabilityDto availabilityDto);

    APIResponse<PaymentRequestDto> addNewBooking(Long tripItemId);

    APIResponse<BookingResponseDTO> getBookingsOnTheDay(AvailabilityDto availabilityDto, Long id);

    APIResponse<List<BookingItemDto>> getAllBookingForTrip(Long tripId);

    APIResponse<List<BookingItemDto>> getBookings(Long serviceId, LocalDateTime from, LocalDateTime to);
}
