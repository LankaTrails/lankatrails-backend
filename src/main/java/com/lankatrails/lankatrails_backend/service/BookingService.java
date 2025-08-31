package com.lankatrails.lankatrails_backend.service;

import com.lankatrails.lankatrails_backend.dtos.AvailabilityDto;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.AvailabilityResponse;
import com.lankatrails.lankatrails_backend.dtos.response.BookingResponseDTO;

public interface BookingService {
    APIResponse<AvailabilityResponse> checkAvailability(AvailabilityDto availabilityDto);
    APIResponse<String> addNewBooking(Long tripItemId);
    APIResponse<BookingResponseDTO> getBookingsOnTheDay(AvailabilityDto availabilityDto, Long id);
}
