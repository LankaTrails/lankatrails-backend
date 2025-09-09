package com.lankatrails.lankatrails_backend.service;

import com.lankatrails.lankatrails_backend.dtos.AvailabilityDto;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.AvailabilityResponse;

public interface AvailabilityService {


    APIResponse<AvailabilityResponse> checkAvailability(AvailabilityDto availabilityDto);
    
    /**
     * Validates basic input parameters for availability checking.
     * 
     * @param availabilityDto The availability request parameters
     * @return APIResponse indicating validation success or failure
     */
    APIResponse<String> validateAvailabilityInput(AvailabilityDto availabilityDto);
    
    /**
     * Validates that the requested time period falls within the service's operating hours
     * for each day in the range.
     * 
     * @param availabilityDto The availability request parameters
     * @return APIResponse indicating time constraint validation success or failure
     */
    APIResponse<String> validateDateTimeConstraints(AvailabilityDto availabilityDto);
    
    /**
     * Validates service availability by checking booking type constraints and capacity.
     * 
     * @param availabilityDto The availability request parameters
     * @return APIResponse indicating service availability validation success or failure
     */
    APIResponse<String> validateServiceAvailability(AvailabilityDto availabilityDto);
}
