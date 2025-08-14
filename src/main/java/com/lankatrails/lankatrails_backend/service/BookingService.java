package com.lankatrails.lankatrails_backend.service;

import com.lankatrails.lankatrails_backend.dtos.request.BookingRequestDTO;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.BookingResponseDTO;
import com.lankatrails.lankatrails_backend.dtos.response.TimeSlotsResponseDTO;

public interface BookingService {
    APIResponse<String> checkTimeSlotAvailability(BookingRequestDTO bookingRequestDTO, Long id);
    APIResponse<String> addNewBooking(BookingRequestDTO bookingRequestDTO, Long id);
    APIResponse<BookingResponseDTO> getBookingsOnTheDay(BookingRequestDTO bookingRequestDTO, Long id);
    APIResponse<TimeSlotsResponseDTO> getTourGuideDaySlots(Long id);
}
