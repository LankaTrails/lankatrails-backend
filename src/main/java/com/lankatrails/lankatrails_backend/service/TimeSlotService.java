package com.lankatrails.lankatrails_backend.service;

import java.time.LocalTime;
import java.util.List;

import com.lankatrails.lankatrails_backend.dtos.AvailabilityDto;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.TimeSlotsResponseDTO;

public interface TimeSlotService {
    
    /**
     * Generate time slots based on open time, close time and slot duration
     * 
     * @param openTime The opening time
     * @param closeTime The closing time
     * @param minutesPerSlot Duration of each slot in minutes
     * @return APIResponse containing list of formatted time slots
     */
    APIResponse<List<String>> generateTimeSlots(LocalTime openTime, LocalTime closeTime, Integer minutesPerSlot);
    
    /**
     * Get all available time slots for a service on current day
     * 
     * @param serviceId The service ID
     * @return APIResponse containing list of formatted time slots
     */
    APIResponse<List<String>> getServiceTimeSlots(Long serviceId);
    
    /**
     * Get all free time slots for a service on a specific day (excluding booked slots)
     * Only processes TIME_SLOTS booking type services
     * 
     * @param availabilityDto The availability request parameters
     * @param serviceId The service ID
     * @return APIResponse containing list of available time slots for booking
     */
    APIResponse<TimeSlotsResponseDTO> getAllFreeTimeSlots(AvailabilityDto availabilityDto, Long serviceId);
    
    /**
     * Get available day slots for a tour guide based on their configuration
     * 
     * @param serviceId The service ID of the tour guide
     * @return APIResponse containing list of formatted time slots for the tour guide
     */
    APIResponse<List<String>> getTourGuideDaySlots(Long serviceId);
    
    /**
     * Get all time slots for a service on a specific date
     * 
     * @param serviceId The service ID
     * @param requestedDate The date for which to get slots (LocalDate)
     * @return APIResponse containing list of formatted time slots
     */
    APIResponse<List<String>> getServiceTimeSlotsForDate(Long serviceId, java.time.LocalDate requestedDate);
}
