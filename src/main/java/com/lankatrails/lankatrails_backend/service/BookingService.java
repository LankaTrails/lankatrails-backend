package com.lankatrails.lankatrails_backend.service;

import java.time.LocalTime;
import java.util.List;

import com.lankatrails.lankatrails_backend.dtos.AvailabilityDto;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.AvailabilityResponse;
import com.lankatrails.lankatrails_backend.dtos.response.BookingResponseDTO;

public interface BookingService {
    APIResponse<AvailabilityResponse> checkAvailability(AvailabilityDto availabilityDto);
    APIResponse<String> addNewBooking(Long tripItemId);
    APIResponse<BookingResponseDTO> getBookingsOnTheDay(AvailabilityDto availabilityDto, Long id);
    APIResponse<List<String>> getTourGuideDaySlots(Long id);
    APIResponse<List<String>> generateTimeSlots( LocalTime openTime, LocalTime closeTime, Integer slotDuration);
    APIResponse<List<String>> getAllFreeTimeSlots(AvailabilityDto availabilityDto, Long id);
    APIResponse<List<String>> getServiceTimeSlots (Long id);
}
