package com.lankatrails.lankatrails_backend.service;

import com.lankatrails.lankatrails_backend.dtos.AvailabilityDto;
import com.lankatrails.lankatrails_backend.dtos.request.AvailabilitySlotDTO;
import com.lankatrails.lankatrails_backend.dtos.request.BookingRequestDTO;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.BookingResponseDTO;
import com.lankatrails.lankatrails_backend.dtos.response.TimeSlotsResponseDTO;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public interface BookingService {
    APIResponse<String> checkAvailability(AvailabilityDto availabilityDto);
    APIResponse<String> validateAvailabilityInput(AvailabilityDto availabilityDto);
    APIResponse<String> validateDateTimeConstraints(AvailabilityDto availabilityDto);
    APIResponse<String> validateServiceAvailability(AvailabilityDto availabilityDto);
    APIResponse<String> addNewBooking(Long tripItemId);
    APIResponse<BookingResponseDTO> getBookingsOnTheDay(AvailabilityDto availabilityDto, Long id);
    APIResponse<List<String>> getTourGuideDaySlots(Long id);
    APIResponse<List<String>> generateTimeSlots( LocalTime openTime, LocalTime closeTime, Long slotDuration);
    APIResponse<List<String>> getAllFreeTimeSlots(AvailabilityDto availabilityDto, Long id);
    APIResponse<List<String>> getServiceTimeSlots (Long id);
}
