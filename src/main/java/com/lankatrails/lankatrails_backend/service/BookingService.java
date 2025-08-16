package com.lankatrails.lankatrails_backend.service;

import com.lankatrails.lankatrails_backend.dtos.request.AvailabilitySlotDTO;
import com.lankatrails.lankatrails_backend.dtos.request.BookingRequestDTO;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.BookingResponseDTO;
import com.lankatrails.lankatrails_backend.dtos.response.TimeSlotsResponseDTO;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;

public interface BookingService {
    APIResponse<String> checkTimeSlotAvailability(BookingRequestDTO bookingRequestDTO, Long id);
    APIResponse<String> addNewBooking(BookingRequestDTO bookingRequestDTO, Long id);
    APIResponse<BookingResponseDTO> getBookingsOnTheDay(BookingRequestDTO bookingRequestDTO, Long id);
    APIResponse<List<String>> getTourGuideDaySlots(Long id);
    APIResponse<List<String>> generateTimeSlots( LocalTime openTime, LocalTime closeTime, Long slotDuration);
}
