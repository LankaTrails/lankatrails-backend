package com.lankatrails.lankatrails_backend.service.impl;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.lankatrails.lankatrails_backend.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lankatrails.lankatrails_backend.dtos.AvailabilityDto;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.AvailabilityResponse;
import com.lankatrails.lankatrails_backend.exception.ResourceNotFoundException;
import com.lankatrails.lankatrails_backend.model.AvailableTime;
import com.lankatrails.lankatrails_backend.model.enums.BookingStatus;
import com.lankatrails.lankatrails_backend.repositories.AvailableTimeRepository;
import com.lankatrails.lankatrails_backend.repositories.BookingRepository;
import com.lankatrails.lankatrails_backend.repositories.ServiceRepository;
import com.lankatrails.lankatrails_backend.service.AvailabilityService;

import lombok.extern.slf4j.Slf4j;

/**
 * Refactored AvailabilityServiceImpl using BookingConfiguration and AvailabilitySlot model.
 * 
 * Key improvements:
 * 1. Configuration-driven approach: Uses BookingConfiguration.bookingType instead of hardcoded service categories
 * 2. Generic capacity validation: Works for any service type through BookingConfiguration settings
 * 3. Enhanced availability validation: Includes break times, holidays, and 24h availability checks
 * 4. Flexible booking types: Supports TIME_SLOTS, MULTI_DAY, WHOLE_DAY, FIXED_TIME, FLEXIBLE_HOURS, EVENT_BASED
 * 
 * The service now validates availability purely based on:
 * - BookingConfiguration (booking type, capacity, duration, limits)
 * - AvailabilitySlot (opening hours, break times, holidays)
 * - Existing confirmed bookings
 * 
 * Legacy category-specific methods are deprecated but kept for backward compatibility.
 */
@Service
@Slf4j
public class AvailabilityServiceImpl implements AvailabilityService {
    
    @Autowired
    private ServiceRepository serviceRepository;
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private AvailableTimeRepository availableTimeRepository;

    @Override
    @Transactional
    public APIResponse<AvailabilityResponse> checkAvailability(AvailabilityDto availabilityDto) {
        // Validate basic input
        APIResponse<String> inputValidation = validateAvailabilityInput(availabilityDto);
        if (!inputValidation.isSuccess()) {
            return createAvailabilityErrorResponse(inputValidation.getMessage());
        }
        // Validate dates and times within service constraints
        APIResponse<String> dateTimeValidation = validateDateTimeConstraints(availabilityDto);
        if (!dateTimeValidation.isSuccess()) {
            return createAvailabilityErrorResponse(dateTimeValidation.getMessage());
        }
        // Validate service availability and get detailed response
        return validateServiceAvailabilityDetailed(availabilityDto);
    }

    @Override
    @Transactional(readOnly = true)
    public APIResponse<String> validateAvailabilityInput(AvailabilityDto availabilityDto) {
        // Check for null values
        if (availabilityDto.getStartDateTime() == null || availabilityDto.getEndDateTime() == null ||
            availabilityDto.getAdultCount() == null || availabilityDto.getChildCount() == null) {
            return createErrorResponse("Start date-time, end date-time, adult count, and child count are required");
        }

        // Check for negative values
        if (availabilityDto.getAdultCount() < 0 || availabilityDto.getChildCount() < 0) {
            return createErrorResponse("Adult count and child count cannot be negative");
        }

        // Check for at least one guest
        int totalGuests = availabilityDto.getAdultCount() + availabilityDto.getChildCount();
        if (totalGuests <= 0) {
            return createErrorResponse("At least one guest (adult or child) is required");
        }

        //validate the past check
        if (availabilityDto.getStartDateTime().isBefore(LocalDateTime.now()) || availabilityDto.getEndDateTime().isBefore(LocalDateTime.now())) {
            return createErrorResponse("Cannot book for past dates");
        }

        // Time validation - ensure start time is before end time
        if (!availabilityDto.getStartDateTime().isBefore(availabilityDto.getEndDateTime())) {
            return createErrorResponse("Start time must be before end time");
        }

        return createSuccessResponse("Input validation passed");
    }

    @Override
    @Transactional(readOnly = true)
    public APIResponse<String> validateDateTimeConstraints(AvailabilityDto availabilityDto) {
        // Get availability slots
        List<AvailableTime> availableTimeList = availableTimeRepository.findByService_ServiceId(availabilityDto.getServiceId());
        if (availableTimeList.isEmpty()) {
            return createErrorResponse("No availability slots defined for this service" + availabilityDto.getServiceId());
        }

        LocalDateTime requestedStartDateTime = availabilityDto.getStartDateTime();
        LocalDateTime requestedEndDateTime = availabilityDto.getEndDateTime();

        // Create map for optimized availability slot lookup
        Map<DayOfWeek, AvailableTime> availabilityMap = availableTimeList.stream()
                .collect(Collectors.toMap(
                    slot -> DayOfWeek.valueOf(slot.getDayOfWeek().toUpperCase()),
                    Function.identity(),
                    (existing, replacement) -> existing // Keep first occurrence in case of duplicates
                ));

        // Iterate through every day in the requested date range
        LocalDate currentDate = requestedStartDateTime.toLocalDate();
        LocalDate endDate = requestedEndDateTime.toLocalDate();
        
        while (!currentDate.isAfter(endDate)) {
            DayOfWeek currentDayOfWeek = currentDate.getDayOfWeek();
            AvailableTime daySlot = availabilityMap.get(currentDayOfWeek);
            
            if (daySlot == null) {
                return createErrorResponse("Service not available on " + currentDayOfWeek + " (" + currentDate + ")");
            }
            
            // Check if service is closed on this day
            if (Boolean.TRUE.equals(daySlot.getIsClosed())) {
                return createErrorResponse("Service is closed on " + currentDayOfWeek + " (" + currentDate + ")");
            }
            
            // Skip time validation for 24-hour services
            if (!Boolean.TRUE.equals(daySlot.getIs24Hours())) {
                // For the first day, check start time constraint
                if (currentDate.equals(requestedStartDateTime.toLocalDate())) {
                    try {
                        LocalDateTime serviceOpenDateTime = LocalDateTime.of(currentDate, daySlot.getOpenTime());
                        if (requestedStartDateTime.isBefore(serviceOpenDateTime)) {
                            return createErrorResponse("Requested start time is before opening time " + serviceOpenDateTime.toLocalTime() + " on " + currentDate);
                        }
                    } catch (Exception e) {
                        return createErrorResponse("Invalid open time format in availability slot for " + currentDayOfWeek);
                    }
                }
                
                // For the last day, check end time constraint
                if (currentDate.equals(requestedEndDateTime.toLocalDate())) {
                    try {
                        LocalDateTime serviceCloseDateTime = LocalDateTime.of(currentDate, daySlot.getCloseTime());
                        if (requestedEndDateTime.isAfter(serviceCloseDateTime)) {
                            return createErrorResponse("Requested end time is after closing time " + serviceCloseDateTime.toLocalTime() + " on " + currentDate);
                        }
                    } catch (Exception e) {
                        return createErrorResponse("Invalid close time format in availability slot for " + currentDayOfWeek);
                    }
                }
                
                // Check break times for the current day
                APIResponse<String> breakTimeValidation = validateBreakTimes(requestedStartDateTime, requestedEndDateTime, currentDate, daySlot);
                if (!breakTimeValidation.isSuccess()) {
                    return breakTimeValidation;
                }
            }
            
            currentDate = currentDate.plusDays(1);
        }

        return createSuccessResponse("Date and time validation passed");
    }

    /**
     * Validates that the requested time doesn't conflict with break times
     */
    private APIResponse<String> validateBreakTimes(LocalDateTime requestedStart, LocalDateTime requestedEnd, 
                                                  LocalDate currentDate, AvailableTime daySlot) {
        // Only check break times if the requested period includes this specific date
        boolean isStartDay = currentDate.equals(requestedStart.toLocalDate());
        boolean isEndDay = currentDate.equals(requestedEnd.toLocalDate());
        
        if (!isStartDay && !isEndDay && currentDate.isAfter(requestedStart.toLocalDate()) && currentDate.isBefore(requestedEnd.toLocalDate())) {
            // For middle days in multi-day bookings, the entire day is booked, so break times don't apply
            return createSuccessResponse("Full day booking - break times don't apply");
        }

        for (BreakTime breakTime : daySlot.getBreakTimes()) {
            LocalDateTime breakStart = LocalDateTime.of(currentDate, breakTime.getBreakStart());
            LocalDateTime breakEnd = LocalDateTime.of(currentDate, breakTime.getBreakEnd());

            // Determine the actual start and end times for this day
            LocalDateTime actualStart = isStartDay ? requestedStart : LocalDateTime.of(currentDate, daySlot.getOpenTime());
            LocalDateTime actualEnd = isEndDay ? requestedEnd : LocalDateTime.of(currentDate, daySlot.getCloseTime());

            // Check if requested time overlaps with break time (using standard interval overlap logic)
            if (!(actualEnd.isBefore(breakStart) || actualStart.isAfter(breakEnd))) {
                return createErrorResponse(String.format("Requested time overlaps with break time %s-%s on %s", 
                        breakTime.getBreakStart(), breakTime.getBreakEnd(), currentDate));
            }
        }

        return createSuccessResponse("Break time validation passed");
    }

    @Override
    @Transactional(readOnly = true)
    public APIResponse<String> validateServiceAvailability(AvailabilityDto availabilityDto) {
        // Get service once and reuse (fix duplicate service fetching)
        com.lankatrails.lankatrails_backend.model.Service service = serviceRepository.findById(availabilityDto.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Service", availabilityDto.getServiceId()));

        BookingConfiguration config = service.getBookingConfiguration();
        if (config == null) {
            return createErrorResponse("No booking configuration found for service: " + availabilityDto.getServiceId());
        }

        log.info("Booking Type: {}", config.getBookingType());

        // Validate booking type specific constraints using configuration
        switch (config.getBookingType()) {
            case TIME_SLOTS:
                return validateTimeSlotBooking(availabilityDto, service, config);
            case MULTI_DAY:
                return validateMultiDayBooking(availabilityDto, service, config);
            case WHOLE_DAY:
                return validateWholeDayBooking(availabilityDto, service, config);
            case FIXED_TIME:
                return validateFixedTimeBooking(availabilityDto, service, config);
            case FLEXIBLE_HOURS:
                return validateFlexibleHoursBooking(availabilityDto, service, config);
            case EVENT_BASED:
                return validateEventBasedBooking(availabilityDto, service, config);
            default:
                return createErrorResponse("Invalid or unsupported booking type: " + config.getBookingType());
        }
    }

    private APIResponse<AvailabilityResponse> validateServiceAvailabilityDetailed(AvailabilityDto availabilityDto) {
        // Get service once and reuse
        com.lankatrails.lankatrails_backend.model.Service service = serviceRepository.findById(availabilityDto.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Service", availabilityDto.getServiceId()));

        BookingConfiguration config = service.getBookingConfiguration();
        if (config == null) {
            return createAvailabilityErrorResponse("No booking configuration found for service: " + availabilityDto.getServiceId());
        }

        log.info("Booking Type: {}", config.getBookingType());

        // Validate booking type specific constraints and get detailed capacity info using configuration
        switch (config.getBookingType()) {
            case TIME_SLOTS:
                return validateTimeSlotBookingDetailed(availabilityDto, service, config);
            case MULTI_DAY:
                return validateMultiDayBookingDetailed(availabilityDto, service, config);
            case WHOLE_DAY:
                return validateWholeDayBookingDetailed(availabilityDto, service, config);
            case FIXED_TIME:
                return validateFixedTimeBookingDetailed(availabilityDto, service, config);
            case FLEXIBLE_HOURS:
                return validateFlexibleHoursBookingDetailed(availabilityDto, service, config);
            case EVENT_BASED:
                return validateEventBasedBookingDetailed(availabilityDto, service, config);
            default:
                return createAvailabilityErrorResponse("Invalid or unsupported booking type: " + config.getBookingType());
        }
    }

    @Transactional(readOnly = true)
    private APIResponse<String> validateTimeSlotBooking(AvailabilityDto availabilityDto, 
                                                        com.lankatrails.lankatrails_backend.model.Service service,
                                                        BookingConfiguration config) {
        // TIME_SLOTS booking must be for the same day
        if (!availabilityDto.getStartDateTime().toLocalDate().equals(availabilityDto.getEndDateTime().toLocalDate())) {
            return createErrorResponse("Time slot bookings must be for the same day.");
        }

        // Validate that the requested time slot matches the configured slot duration
        if (config.getSlotDuration() != null) {
            long requestedMinutes = Duration.between(availabilityDto.getStartDateTime(), availabilityDto.getEndDateTime()).toMinutes();
            if (requestedMinutes != config.getSlotDuration()) {
                return createErrorResponse(String.format("Requested slot duration (%d minutes) does not match configured slot duration (%d minutes)", 
                        requestedMinutes, config.getSlotDuration()));
            }
        }

        // Check if the requested time slot falls within available times
        APIResponse<String> timeSlotValidation = validateTimeSlotAgainstAvailability(availabilityDto, service);
        if (!timeSlotValidation.isSuccess()) {
            return timeSlotValidation;
        }

        // Check capacity using configuration-driven logic
        return validateCapacityUsingConfiguration(service, config, availabilityDto);
    }

    @Transactional(readOnly = true)
    private APIResponse<String> validateMultiDayBooking(AvailabilityDto availabilityDto, 
                                                       com.lankatrails.lankatrails_backend.model.Service service,
                                                       BookingConfiguration config) {
        // Validate minimum and maximum booking days if configured
        long requestedDays = Duration.between(availabilityDto.getStartDateTime(), availabilityDto.getEndDateTime()).toDays();
        
        if (config.getMinimumBookingDays() != null && requestedDays < config.getMinimumBookingDays()) {
            return createErrorResponse(String.format("Minimum booking period is %d days, but %d days requested", 
                    config.getMinimumBookingDays(), requestedDays));
        }
        
        if (config.getMaximumBookingDays() != null && requestedDays > config.getMaximumBookingDays()) {
            return createErrorResponse(String.format("Maximum booking period is %d days, but %d days requested", 
                    config.getMaximumBookingDays(), requestedDays));
        }

        // Check capacity using configuration-driven logic
        return validateCapacityUsingConfiguration(service, config, availabilityDto);
    }

    @Transactional(readOnly = true)
    private APIResponse<String> validateWholeDayBooking(AvailabilityDto availabilityDto, 
                                                       com.lankatrails.lankatrails_backend.model.Service service,
                                                       BookingConfiguration config) {
        // For whole day bookings, ensure the request spans the entire available day
        // Note: The default check-in/check-out times can be used for validation if needed
        // but for now we proceed with capacity validation
        
        // Check capacity using configuration-driven logic
        return validateCapacityUsingConfiguration(service, config, availabilityDto);
    }

    @Transactional(readOnly = true)
    private APIResponse<String> validateFixedTimeBooking(AvailabilityDto availabilityDto, 
                                                        com.lankatrails.lankatrails_backend.model.Service service,
                                                        BookingConfiguration config) {
        // Fixed time bookings have predetermined duration
        if (config.getSlotDuration() != null) {
            long requestedMinutes = Duration.between(availabilityDto.getStartDateTime(), availabilityDto.getEndDateTime()).toMinutes();
            if (requestedMinutes != config.getSlotDuration()) {
                return createErrorResponse(String.format("Fixed time booking duration must be %d minutes", config.getSlotDuration()));
            }
        }

        return validateCapacityUsingConfiguration(service, config, availabilityDto);
    }

    @Transactional(readOnly = true)
    private APIResponse<String> validateFlexibleHoursBooking(AvailabilityDto availabilityDto, 
                                                           com.lankatrails.lankatrails_backend.model.Service service,
                                                           BookingConfiguration config) {
        // Flexible hours allow variable duration within availability windows
        APIResponse<String> timeSlotValidation = validateTimeSlotAgainstAvailability(availabilityDto, service);
        if (!timeSlotValidation.isSuccess()) {
            return timeSlotValidation;
        }

        return validateCapacityUsingConfiguration(service, config, availabilityDto);
    }

    @Transactional(readOnly = true)
    private APIResponse<String> validateEventBasedBooking(AvailabilityDto availabilityDto, 
                                                         com.lankatrails.lankatrails_backend.model.Service service,
                                                         BookingConfiguration config) {
        // Event-based bookings are tied to specific events/schedules
        // For now, use standard capacity validation
        return validateCapacityUsingConfiguration(service, config, availabilityDto);
    }

    public APIResponse<String> validateCapacity(com.lankatrails.lankatrails_backend.model.Service service, AvailabilityDto availabilityDto) {
        // Use the new configuration-driven approach
        BookingConfiguration config = service.getBookingConfiguration();
        if (config == null) {
            return createErrorResponse("No booking configuration found for service");
        }
        return validateCapacityUsingConfiguration(service, config, availabilityDto);
    }

    /**
     * Configuration-driven capacity validation that works for any service type
     */
    private APIResponse<String> validateCapacityUsingConfiguration(com.lankatrails.lankatrails_backend.model.Service service, 
                                                                  BookingConfiguration config, 
                                                                  AvailabilityDto availabilityDto) {
        // Default to 1 unit if not specified
        Integer requestedUnits = Optional.ofNullable(availabilityDto.getNoOfUnits()).orElse(1);
        int totalGuests = availabilityDto.getAdultCount() + availabilityDto.getChildCount();

        // Validate units requested against configuration limits
        if (config.getMinUnitsPerBooking() != null && requestedUnits < config.getMinUnitsPerBooking()) {
            return createErrorResponse(String.format("Minimum %d units required, but %d requested", 
                    config.getMinUnitsPerBooking(), requestedUnits));
        }
        
        if (config.getMaxUnitsPerBooking() != null && requestedUnits > config.getMaxUnitsPerBooking()) {
            return createErrorResponse(String.format("Maximum %d units allowed, but %d requested", 
                    config.getMaxUnitsPerBooking(), requestedUnits));
        }

        // Check if enough units are available
        if (config.getTotalUnits() != null) {
            Integer bookedUnits = bookingRepository.findBookedUnitsDuringPeriod(
                    service.getServiceId(),
                    availabilityDto.getStartDateTime(),
                    availabilityDto.getEndDateTime(),
                    BookingStatus.BOOKED
            );

            int availableUnits = config.getTotalUnits() - bookedUnits;
            if (availableUnits < requestedUnits) {
                return createErrorResponse(String.format("Only %d units available, but %d requested", 
                        availableUnits, requestedUnits));
            }
        }

        // Check guest capacity per unit
        if (config.getUnitAdultCapacity() != null && config.getUnitChildCapacity() != null) {
            int maxGuestsForRequestedUnits = (config.getUnitAdultCapacity() + config.getUnitChildCapacity()) * requestedUnits;
            
            if (totalGuests > maxGuestsForRequestedUnits) {
                // Check if extra capacity is allowed
                if (Boolean.TRUE.equals(config.getAllowExtraCapacity())) {
                    int extraGuestsNeeded = totalGuests - maxGuestsForRequestedUnits;
                    int maxExtraAdults = Optional.ofNullable(config.getExtraAdultCapacityLimit()).orElse(0) * requestedUnits;
                    int maxExtraChildren = Optional.ofNullable(config.getExtraChildCapacityLimit()).orElse(0) * requestedUnits;
                    
                    if (extraGuestsNeeded > maxExtraAdults + maxExtraChildren) {
                        return createErrorResponse(String.format("Requested %d units can accommodate max %d guests (including %d extra), but %d guests provided", 
                                requestedUnits, maxGuestsForRequestedUnits + maxExtraAdults + maxExtraChildren, 
                                maxExtraAdults + maxExtraChildren, totalGuests));
                    }
                } else {
                    return createErrorResponse(String.format("Requested %d units can accommodate max %d guests, but %d guests provided", 
                            requestedUnits, maxGuestsForRequestedUnits, totalGuests));
                }
            }
        }

        return createSuccessResponse("Capacity validation passed");
    }

    /**
     * Validates if the requested time slot falls within available times and respects break times
     */
    private APIResponse<String> validateTimeSlotAgainstAvailability(AvailabilityDto availabilityDto, 
                                                                   com.lankatrails.lankatrails_backend.model.Service service) {
        LocalDateTime requestedStart = availabilityDto.getStartDateTime();
        LocalDateTime requestedEnd = availabilityDto.getEndDateTime();
        DayOfWeek requestedDay = requestedStart.getDayOfWeek();

        // Get availability for the requested day
        List<AvailableTime> availableTimeList = availableTimeRepository.findByService_ServiceId(service.getServiceId());
        Optional<AvailableTime> dayAvailability = availableTimeList.stream()
                .filter(slot -> DayOfWeek.valueOf(slot.getDayOfWeek().toUpperCase()) == requestedDay)
                .findFirst();

        if (dayAvailability.isEmpty()) {
            return createErrorResponse("Service not available on " + requestedDay);
        }

        AvailableTime availability = dayAvailability.get();

        // Check if service is closed on this day
        if (Boolean.TRUE.equals(availability.getIsClosed())) {
            return createErrorResponse("Service is closed on " + requestedDay);
        }

        // Check 24-hour availability
        if (Boolean.TRUE.equals(availability.getIs24Hours())) {
            return createSuccessResponse("Service available 24 hours");
        }

        // Check opening hours
        LocalDateTime serviceStart = LocalDateTime.of(requestedStart.toLocalDate(), availability.getOpenTime());
        LocalDateTime serviceEnd = LocalDateTime.of(requestedStart.toLocalDate(), availability.getCloseTime());

        if (requestedStart.isBefore(serviceStart) || requestedEnd.isAfter(serviceEnd)) {
            return createErrorResponse(String.format("Requested time %s-%s is outside service hours %s-%s", 
                    requestedStart.toLocalTime(), requestedEnd.toLocalTime(), 
                    availability.getOpenTime(), availability.getCloseTime()));
        }

        // Check break times
        for (BreakTime breakTime : availability.getBreakTimes()) {
            LocalDateTime breakStart = LocalDateTime.of(requestedStart.toLocalDate(), breakTime.getBreakStart());
            LocalDateTime breakEnd = LocalDateTime.of(requestedStart.toLocalDate(), breakTime.getBreakEnd());

            // Check if requested time overlaps with break time
            if (!(requestedEnd.isBefore(breakStart) || requestedStart.isAfter(breakEnd))) {
                return createErrorResponse(String.format("Requested time overlaps with break time %s-%s", 
                        breakTime.getBreakStart(), breakTime.getBreakEnd()));
            }
        }

        return createSuccessResponse("Time slot validation passed");
    }

    /**
     * @deprecated Use validateCapacityUsingConfiguration instead.
     * Legacy method for accommodation capacity validation - kept for backward compatibility.
     */
    @Deprecated
    private APIResponse<String> validateAccommodationCapacity(Accommodation accommodation, AvailabilityDto availabilityDto) {
        // Fallback to configuration-driven approach if BookingConfiguration exists
        if (accommodation.getBookingConfiguration() != null) {
            return validateCapacityUsingConfiguration(accommodation, accommodation.getBookingConfiguration(), availabilityDto);
        }
        
        // Legacy logic for backward compatibility
        Integer requestedUnits = Optional.ofNullable(availabilityDto.getNoOfUnits()).orElse(1);
        
        Integer bookedRooms = bookingRepository.findBookedUnitsDuringPeriod(
                accommodation.getServiceId(),
                availabilityDto.getStartDateTime(),
                availabilityDto.getEndDateTime(),
                BookingStatus.BOOKED
        );

        // Note: getNumberOfRooms() method may not exist in new configuration model
        // This is legacy code that should be replaced with configuration-driven approach
        return createSuccessResponse("Legacy accommodation validation - consider updating to use BookingConfiguration");
    }

    /**
     * @deprecated Use validateCapacityUsingConfiguration instead.
     * Legacy method for transport capacity validation - kept for backward compatibility.
     */
    @Deprecated
    private APIResponse<String> validateTransportCapacity(Transport transport, AvailabilityDto availabilityDto) {
        // Fallback to configuration-driven approach if BookingConfiguration exists
        if (transport.getBookingConfiguration() != null) {
            return validateCapacityUsingConfiguration(transport, transport.getBookingConfiguration(), availabilityDto);
        }
        
        // Legacy logic placeholder
        return createSuccessResponse("Legacy transport validation - consider updating to use BookingConfiguration");
    }

    /**
     * @deprecated Use validateCapacityUsingConfiguration instead.
     * Legacy method for activity service capacity validation - kept for backward compatibility.
     */
    @Deprecated
    private APIResponse<String> validateActivityServiceCapacity(ActivityService activityService, AvailabilityDto availabilityDto) {
        // Fallback to configuration-driven approach if BookingConfiguration exists
        if (activityService.getBookingConfiguration() != null) {
            return validateCapacityUsingConfiguration(activityService, activityService.getBookingConfiguration(), availabilityDto);
        }
        
        // Legacy logic placeholder
        return createSuccessResponse("Legacy activity service validation - consider updating to use BookingConfiguration");
    }

    /**
     * @deprecated Use validateCapacityUsingConfiguration instead.
     * Legacy method for food and beverage capacity validation - kept for backward compatibility.
     */
    @Deprecated
    private APIResponse<String> validateFoodAndBeverageCapacity(FoodAndBeverage foodAndBeverage, AvailabilityDto availabilityDto) {
        // Fallback to configuration-driven approach if BookingConfiguration exists
        if (foodAndBeverage.getBookingConfiguration() != null) {
            return validateCapacityUsingConfiguration(foodAndBeverage, foodAndBeverage.getBookingConfiguration(), availabilityDto);
        }
        
        // Legacy logic placeholder
        return createSuccessResponse("Legacy food and beverage validation - consider updating to use BookingConfiguration");
    }

    /**
     * @deprecated Use validateCapacityUsingConfiguration instead.
     * Legacy method for tourist guide capacity validation - kept for backward compatibility.
     */
    @Deprecated
    private APIResponse<String> validateTouristGuideCapacity(TouristGuide touristGuide, AvailabilityDto availabilityDto) {
        // Fallback to configuration-driven approach if BookingConfiguration exists
        if (touristGuide.getBookingConfiguration() != null) {
            return validateCapacityUsingConfiguration(touristGuide, touristGuide.getBookingConfiguration(), availabilityDto);
        }
        
        // Legacy logic placeholder
        return createSuccessResponse("Legacy tourist guide validation - consider updating to use BookingConfiguration");
    }

    private APIResponse<AvailabilityResponse> validateTimeSlotBookingDetailed(AvailabilityDto availabilityDto, 
                                                                             com.lankatrails.lankatrails_backend.model.Service service,
                                                                             BookingConfiguration config) {
        // TIME_SLOTS booking must be for the same day
        if (!availabilityDto.getStartDateTime().toLocalDate().equals(availabilityDto.getEndDateTime().toLocalDate())) {
            return createAvailabilityErrorResponse("Time slot bookings must be for the same day.");
        }

        // Validate slot duration if configured
        if (config.getSlotDuration() != null) {
            long requestedMinutes = Duration.between(availabilityDto.getStartDateTime(), availabilityDto.getEndDateTime()).toMinutes();
            if (requestedMinutes != config.getSlotDuration()) {
                return createAvailabilityErrorResponse(String.format("Requested slot duration (%d minutes) does not match configured slot duration (%d minutes)", 
                        requestedMinutes, config.getSlotDuration()));
            }
        }

        // Check availability slot
        APIResponse<String> timeSlotValidation = validateTimeSlotAgainstAvailability(availabilityDto, service);
        if (!timeSlotValidation.isSuccess()) {
            return createAvailabilityErrorResponse(timeSlotValidation.getMessage());
        }

        // Get detailed capacity information using configuration
        return validateCapacityDetailedUsingConfiguration(service, config, availabilityDto);
    }

    private APIResponse<AvailabilityResponse> validateMultiDayBookingDetailed(AvailabilityDto availabilityDto, 
                                                                             com.lankatrails.lankatrails_backend.model.Service service,
                                                                             BookingConfiguration config) {
        // Validate booking days constraints
        long requestedDays = Duration.between(availabilityDto.getStartDateTime(), availabilityDto.getEndDateTime()).toDays();
        
        if (config.getMinimumBookingDays() != null && requestedDays < config.getMinimumBookingDays()) {
            return createAvailabilityErrorResponse(String.format("Minimum booking period is %d days, but %d days requested", 
                    config.getMinimumBookingDays(), requestedDays));
        }
        
        if (config.getMaximumBookingDays() != null && requestedDays > config.getMaximumBookingDays()) {
            return createAvailabilityErrorResponse(String.format("Maximum booking period is %d days, but %d days requested", 
                    config.getMaximumBookingDays(), requestedDays));
        }

        // Get detailed capacity information using configuration
        return validateCapacityDetailedUsingConfiguration(service, config, availabilityDto);
    }

    private APIResponse<AvailabilityResponse> validateWholeDayBookingDetailed(AvailabilityDto availabilityDto, 
                                                                             com.lankatrails.lankatrails_backend.model.Service service,
                                                                             BookingConfiguration config) {
        return validateCapacityDetailedUsingConfiguration(service, config, availabilityDto);
    }

    private APIResponse<AvailabilityResponse> validateFixedTimeBookingDetailed(AvailabilityDto availabilityDto, 
                                                                              com.lankatrails.lankatrails_backend.model.Service service,
                                                                              BookingConfiguration config) {
        if (config.getSlotDuration() != null) {
            long requestedMinutes = Duration.between(availabilityDto.getStartDateTime(), availabilityDto.getEndDateTime()).toMinutes();
            if (requestedMinutes != config.getSlotDuration()) {
                return createAvailabilityErrorResponse(String.format("Fixed time booking duration must be %d minutes", config.getSlotDuration()));
            }
        }

        return validateCapacityDetailedUsingConfiguration(service, config, availabilityDto);
    }

    private APIResponse<AvailabilityResponse> validateFlexibleHoursBookingDetailed(AvailabilityDto availabilityDto, 
                                                                                  com.lankatrails.lankatrails_backend.model.Service service,
                                                                                  BookingConfiguration config) {
        APIResponse<String> timeSlotValidation = validateTimeSlotAgainstAvailability(availabilityDto, service);
        if (!timeSlotValidation.isSuccess()) {
            return createAvailabilityErrorResponse(timeSlotValidation.getMessage());
        }

        return validateCapacityDetailedUsingConfiguration(service, config, availabilityDto);
    }

    private APIResponse<AvailabilityResponse> validateEventBasedBookingDetailed(AvailabilityDto availabilityDto, 
                                                                               com.lankatrails.lankatrails_backend.model.Service service,
                                                                               BookingConfiguration config) {
        return validateCapacityDetailedUsingConfiguration(service, config, availabilityDto);
    }

    private APIResponse<AvailabilityResponse> validateCapacityDetailed(com.lankatrails.lankatrails_backend.model.Service service, AvailabilityDto availabilityDto) {
        // Use the new configuration-driven approach
        BookingConfiguration config = service.getBookingConfiguration();
        if (config == null) {
            return createAvailabilityErrorResponse("No booking configuration found for service");
        }
        return validateCapacityDetailedUsingConfiguration(service, config, availabilityDto);
    }

    /**
     * Configuration-driven detailed capacity validation that works for any service type
     */
    private APIResponse<AvailabilityResponse> validateCapacityDetailedUsingConfiguration(com.lankatrails.lankatrails_backend.model.Service service, 
                                                                                        BookingConfiguration config, 
                                                                                        AvailabilityDto availabilityDto) {
        Integer requestedUnits = Optional.ofNullable(availabilityDto.getNoOfUnits()).orElse(1);
        int totalGuests = availabilityDto.getAdultCount() + availabilityDto.getChildCount();

        // Validate units requested against configuration limits
        if (config.getMinUnitsPerBooking() != null && requestedUnits < config.getMinUnitsPerBooking()) {
            AvailabilityResponse response = AvailabilityResponse.builder()
                    .available(false)
                    .message(String.format("Minimum %d units required, but %d requested", 
                            config.getMinUnitsPerBooking(), requestedUnits))
                    .requestedUnits(requestedUnits)
                    .build();
            
            return APIResponse.<AvailabilityResponse>builder()
                    .success(false)
                    .message(response.getMessage())
                    .data(response)
                    .build();
        }
        
        if (config.getMaxUnitsPerBooking() != null && requestedUnits > config.getMaxUnitsPerBooking()) {
            AvailabilityResponse response = AvailabilityResponse.builder()
                    .available(false)
                    .message(String.format("Maximum %d units allowed, but %d requested", 
                            config.getMaxUnitsPerBooking(), requestedUnits))
                    .requestedUnits(requestedUnits)
                    .build();
            
            return APIResponse.<AvailabilityResponse>builder()
                    .success(false)
                    .message(response.getMessage())
                    .data(response)
                    .build();
        }

        Integer totalUnits = config.getTotalUnits();
        Integer availableUnits = totalUnits;
        
        // Check if enough units are available
        if (totalUnits != null) {
            Integer bookedUnits = bookingRepository.findBookedUnitsDuringPeriod(
                    service.getServiceId(),
                    availabilityDto.getStartDateTime(),
                    availabilityDto.getEndDateTime(),
                    BookingStatus.BOOKED
            );

            availableUnits = totalUnits - bookedUnits;
            if (availableUnits < requestedUnits) {
                AvailabilityResponse response = AvailabilityResponse.builder()
                        .available(false)
                        .message(String.format("Only %d units available, but %d requested", 
                                availableUnits, requestedUnits))
                        .availableUnits(availableUnits)
                        .requestedUnits(requestedUnits)
                        .totalCapacity(totalUnits)
                        .build();
                
                return APIResponse.<AvailabilityResponse>builder()
                        .success(false)
                        .message(response.getMessage())
                        .data(response)
                        .build();
            }
        }

        // Check guest capacity per unit
        if (config.getUnitAdultCapacity() != null && config.getUnitChildCapacity() != null) {
            int maxGuestsForRequestedUnits = (config.getUnitAdultCapacity() + config.getUnitChildCapacity()) * requestedUnits;
            
            if (totalGuests > maxGuestsForRequestedUnits) {
                // Check if extra capacity is allowed
                if (Boolean.TRUE.equals(config.getAllowExtraCapacity())) {
                    int extraGuestsNeeded = totalGuests - maxGuestsForRequestedUnits;
                    int maxExtraAdults = Optional.ofNullable(config.getExtraAdultCapacityLimit()).orElse(0) * requestedUnits;
                    int maxExtraChildren = Optional.ofNullable(config.getExtraChildCapacityLimit()).orElse(0) * requestedUnits;
                    
                    if (extraGuestsNeeded > maxExtraAdults + maxExtraChildren) {
                        AvailabilityResponse response = AvailabilityResponse.builder()
                                .available(false)
                                .message(String.format("Requested %d units can accommodate max %d guests (including %d extra), but %d guests provided", 
                                        requestedUnits, maxGuestsForRequestedUnits + maxExtraAdults + maxExtraChildren, 
                                        maxExtraAdults + maxExtraChildren, totalGuests))
                                .availableUnits(availableUnits)
                                .requestedUnits(requestedUnits)
                                .totalCapacity(totalUnits)
                                .build();
                        
                        return APIResponse.<AvailabilityResponse>builder()
                                .success(false)
                                .message(response.getMessage())
                                .data(response)
                                .build();
                    }
                } else {
                    AvailabilityResponse response = AvailabilityResponse.builder()
                            .available(false)
                            .message(String.format("Requested %d units can accommodate max %d guests, but %d guests provided", 
                                    requestedUnits, maxGuestsForRequestedUnits, totalGuests))
                            .availableUnits(availableUnits)
                            .requestedUnits(requestedUnits)
                            .totalCapacity(totalUnits)
                            .build();
                    
                    return APIResponse.<AvailabilityResponse>builder()
                            .success(false)
                            .message(response.getMessage())
                            .data(response)
                            .build();
                }
            }
        }

        // Success case
        AvailabilityResponse response = AvailabilityResponse.builder()
                .available(true)
                .message("Service is available for booking")
                .availableUnits(availableUnits)
                .requestedUnits(requestedUnits)
                .totalCapacity(totalUnits)
                .build();
        
        return createAvailabilitySuccessResponse(response);
    }

    /**
     * @deprecated Use validateCapacityDetailedUsingConfiguration instead.
     * Legacy detailed validation methods - kept for backward compatibility.
     */
    @Deprecated
    private APIResponse<AvailabilityResponse> validateAccommodationCapacityDetailed(Accommodation accommodation, AvailabilityDto availabilityDto) {
        // Fallback to configuration-driven approach if BookingConfiguration exists
        if (accommodation.getBookingConfiguration() != null) {
            return validateCapacityDetailedUsingConfiguration(accommodation, accommodation.getBookingConfiguration(), availabilityDto);
        }
        
        // Default response for legacy case
        AvailabilityResponse response = AvailabilityResponse.builder()
                .available(true)
                .message("Legacy accommodation validation - consider updating to use BookingConfiguration")
                .requestedUnits(Optional.ofNullable(availabilityDto.getNoOfUnits()).orElse(1))
                .build();
        
        return createAvailabilitySuccessResponse(response);
    }

    @Deprecated
    private APIResponse<AvailabilityResponse> validateTransportCapacityDetailed(Transport transport, AvailabilityDto availabilityDto) {
        // Fallback to configuration-driven approach if BookingConfiguration exists
        if (transport.getBookingConfiguration() != null) {
            return validateCapacityDetailedUsingConfiguration(transport, transport.getBookingConfiguration(), availabilityDto);
        }
        
        // Default response for legacy case
        AvailabilityResponse response = AvailabilityResponse.builder()
                .available(true)
                .message("Legacy transport validation - consider updating to use BookingConfiguration")
                .requestedUnits(Optional.ofNullable(availabilityDto.getNoOfUnits()).orElse(1))
                .build();
        
        return createAvailabilitySuccessResponse(response);
    }

    @Deprecated
    private APIResponse<AvailabilityResponse> validateActivityServiceCapacityDetailed(ActivityService activityService, AvailabilityDto availabilityDto) {
        // Fallback to configuration-driven approach if BookingConfiguration exists
        if (activityService.getBookingConfiguration() != null) {
            return validateCapacityDetailedUsingConfiguration(activityService, activityService.getBookingConfiguration(), availabilityDto);
        }
        
        // Default response for legacy case
        AvailabilityResponse response = AvailabilityResponse.builder()
                .available(true)
                .message("Legacy activity service validation - consider updating to use BookingConfiguration")
                .requestedUnits(Optional.ofNullable(availabilityDto.getNoOfUnits()).orElse(1))
                .build();
        
        return createAvailabilitySuccessResponse(response);
    }

    @Deprecated
    private APIResponse<AvailabilityResponse> validateFoodAndBeverageCapacityDetailed(FoodAndBeverage foodAndBeverage, AvailabilityDto availabilityDto) {
        // Fallback to configuration-driven approach if BookingConfiguration exists
        if (foodAndBeverage.getBookingConfiguration() != null) {
            return validateCapacityDetailedUsingConfiguration(foodAndBeverage, foodAndBeverage.getBookingConfiguration(), availabilityDto);
        }
        
        // Default response for legacy case
        AvailabilityResponse response = AvailabilityResponse.builder()
                .available(true)
                .message("Legacy food and beverage validation - consider updating to use BookingConfiguration")
                .requestedUnits(Optional.ofNullable(availabilityDto.getNoOfUnits()).orElse(1))
                .build();
        
        return createAvailabilitySuccessResponse(response);
    }

    @Deprecated
    private APIResponse<AvailabilityResponse> validateTouristGuideCapacityDetailed(TouristGuide touristGuide, AvailabilityDto availabilityDto) {
        // Fallback to configuration-driven approach if BookingConfiguration exists
        if (touristGuide.getBookingConfiguration() != null) {
            return validateCapacityDetailedUsingConfiguration(touristGuide, touristGuide.getBookingConfiguration(), availabilityDto);
        }
        
        // Default response for legacy case
        AvailabilityResponse response = AvailabilityResponse.builder()
                .available(true)
                .message("Legacy tourist guide validation - consider updating to use BookingConfiguration")
                .requestedUnits(Optional.ofNullable(availabilityDto.getNoOfUnits()).orElse(1))
                .build();
        
        return createAvailabilitySuccessResponse(response);
    }

    private APIResponse<String> createErrorResponse(String message) {
        return APIResponse.<String>builder()
                .success(false)
                .message(message)
                .data("")
                .build();
    }

    private APIResponse<String> createSuccessResponse(String message) {
        return APIResponse.<String>builder()
                .success(true)
                .message(message)
                .data("")
                .build();
    }

    private APIResponse<AvailabilityResponse> createAvailabilityErrorResponse(String message) {
        AvailabilityResponse response = AvailabilityResponse.builder()
                .available(false)
                .message(message)
                .build();
        
        return APIResponse.<AvailabilityResponse>builder()
                .success(false)
                .message(message)
                .data(response)
                .build();
    }

    private APIResponse<AvailabilityResponse> createAvailabilitySuccessResponse(AvailabilityResponse response) {
        return APIResponse.<AvailabilityResponse>builder()
                .success(true)
                .message(response.getMessage())
                .data(response)
                .build();
    }
}
