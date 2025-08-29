package com.lankatrails.lankatrails_backend.service.impl;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lankatrails.lankatrails_backend.dtos.AvailabilityDto;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.AvailabilityResponse;
import com.lankatrails.lankatrails_backend.exception.ResourceNotFoundException;
import com.lankatrails.lankatrails_backend.model.Accommodation;
import com.lankatrails.lankatrails_backend.model.ActivityService;
import com.lankatrails.lankatrails_backend.model.AvailabilitySlot;
import com.lankatrails.lankatrails_backend.model.FoodAndBeverage;
import com.lankatrails.lankatrails_backend.model.TouristGuide;
import com.lankatrails.lankatrails_backend.model.Transport;
import com.lankatrails.lankatrails_backend.model.enums.BookingStatus;
import com.lankatrails.lankatrails_backend.model.enums.BookingType;
import com.lankatrails.lankatrails_backend.repositories.AvailabilitySlotRepository;
import com.lankatrails.lankatrails_backend.repositories.BookingRepository;
import com.lankatrails.lankatrails_backend.repositories.ServiceRepository;
import com.lankatrails.lankatrails_backend.service.AvailabilityService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AvailabilityServiceImpl implements AvailabilityService {
    
    @Autowired
    private ServiceRepository serviceRepository;
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private AvailabilitySlotRepository availabilitySlotRepository;

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
        List<AvailabilitySlot> availabilitySlotList = availabilitySlotRepository.findByService_ServiceId(availabilityDto.getServiceId());
        if (availabilitySlotList.isEmpty()) {
            return createErrorResponse("No availability slots defined for this service" + availabilityDto.getServiceId());
        }

        LocalDateTime requestedStartDateTime = availabilityDto.getStartDateTime();
        LocalDateTime requestedEndDateTime = availabilityDto.getEndDateTime();

        // Create map for optimized availability slot lookup
        Map<DayOfWeek, AvailabilitySlot> availabilityMap = availabilitySlotList.stream()
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
            AvailabilitySlot daySlot = availabilityMap.get(currentDayOfWeek);
            
            if (daySlot == null) {
                return createErrorResponse("Service not available on " + currentDayOfWeek + " (" + currentDate + ")");
            }
            
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
            
            currentDate = currentDate.plusDays(1);
        }

        return createSuccessResponse("Date and time validation passed");
    }

    @Override
    @Transactional(readOnly = true)
    public APIResponse<String> validateServiceAvailability(AvailabilityDto availabilityDto) {
        // Get service once and reuse (fix duplicate service fetching)
        com.lankatrails.lankatrails_backend.model.Service service = serviceRepository.findById(availabilityDto.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Service", availabilityDto.getServiceId()));

        log.info("Booking Type: {}", service.getBookingType());

        // Validate booking type specific constraints
        if (service.getBookingType() == BookingType.TIME_SLOTS) {
            return validateTimeSlotBooking(availabilityDto, service);
        } else if (service.getBookingType() == BookingType.MULTI_DAY) {
            return validateMultiDayBooking(availabilityDto, service);
        } else {
            return createErrorResponse("Invalid or unsupported booking type: " + service.getBookingType());
        }
    }

    private APIResponse<AvailabilityResponse> validateServiceAvailabilityDetailed(AvailabilityDto availabilityDto) {
        // Get service once and reuse
        com.lankatrails.lankatrails_backend.model.Service service = serviceRepository.findById(availabilityDto.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Service", availabilityDto.getServiceId()));

        log.info("Booking Type: {}", service.getBookingType());

        // Validate booking type specific constraints and get detailed capacity info
        if (service.getBookingType() == BookingType.TIME_SLOTS) {
            return validateTimeSlotBookingDetailed(availabilityDto, service);
        } else if (service.getBookingType() == BookingType.MULTI_DAY) {
            return validateMultiDayBookingDetailed(availabilityDto, service);
        } else {
            return createAvailabilityErrorResponse("Invalid or unsupported booking type: " + service.getBookingType());
        }
    }

    @Transactional(readOnly = true)
    private APIResponse<String> validateTimeSlotBooking(AvailabilityDto availabilityDto, com.lankatrails.lankatrails_backend.model.Service service) {
        // TIME_SLOTS booking must be for the same day
        if (!availabilityDto.getStartDateTime().toLocalDate().equals(availabilityDto.getEndDateTime().toLocalDate())) {
            return createErrorResponse("Time slot bookings must be for the same day.");
        }

        // The capacity validation is sufficient to handle overlaps and availability.
        // No need for a separate findExactConflictingBookings call here.
        APIResponse<String> capacityResponse = validateCapacity(service, availabilityDto);
        if (capacityResponse.isSuccess()) {
            return createSuccessResponse("Available for Time Slot Booking");
        }
        return capacityResponse;
    }

    @Transactional(readOnly = true)
    private APIResponse<String> validateMultiDayBooking(AvailabilityDto availabilityDto, com.lankatrails.lankatrails_backend.model.Service service) {
        // For multi-day bookings, capacity validation handles availability properly
        // by checking booked units vs total capacity during the period
        APIResponse<String> capacityResponse = validateCapacity(service, availabilityDto);
        if (capacityResponse.isSuccess()) {
            return createSuccessResponse("Available for Multi-Day Booking");
        }
        return capacityResponse;
    }

    public APIResponse<String> validateCapacity(com.lankatrails.lankatrails_backend.model.Service service, AvailabilityDto availabilityDto) {
        if (service instanceof Accommodation) {
            return validateAccommodationCapacity((Accommodation) service, availabilityDto);
        } else if (service instanceof Transport) {
            return validateTransportCapacity((Transport) service, availabilityDto);
        } else if (service instanceof ActivityService) {
            return validateActivityServiceCapacity((ActivityService) service, availabilityDto);
        } else if (service instanceof FoodAndBeverage) {
            return validateFoodAndBeverageCapacity((FoodAndBeverage) service, availabilityDto);
        } else if (service instanceof TouristGuide) {
            return validateTouristGuideCapacity((TouristGuide) service, availabilityDto);
        }
        return createSuccessResponse("Capacity validation passed");
    }

    private APIResponse<String> validateAccommodationCapacity(Accommodation accommodation, AvailabilityDto availabilityDto) {
        // Default to 1 unit if not specified (null-safe)
        Integer requestedUnits = Optional.ofNullable(availabilityDto.getNoOfUnits()).orElse(1);
        
        // Calculate already booked rooms for the period
        Integer bookedRooms = bookingRepository.findBookedUnitsDuringPeriod(
                accommodation.getServiceId(),
                availabilityDto.getStartDateTime(),
                availabilityDto.getEndDateTime(),
                BookingStatus.BOOKED
        );

        // Check if enough rooms are available
        int availableRooms = accommodation.getNumberOfRooms() - bookedRooms;
        if (availableRooms < requestedUnits) {
            return createErrorResponse(String.format("Only %d rooms available, but %d requested", 
                    availableRooms, requestedUnits));
        }

        // Check guest capacity
        int totalGuests = availabilityDto.getAdultCount() + availabilityDto.getChildCount();
        int maxGuestsForRequestedRooms = accommodation.getMaxGuests() * requestedUnits;
        if (maxGuestsForRequestedRooms < totalGuests) {
            return createErrorResponse(String.format("Requested %d rooms can accommodate max %d guests, but %d guests provided", 
                    requestedUnits, maxGuestsForRequestedRooms, totalGuests));
        }

        return createSuccessResponse("Accommodation capacity validation passed");
    }

    private APIResponse<String> validateTransportCapacity(Transport transport, AvailabilityDto availabilityDto) {
        // Default to 1 unit if not specified (null-safe)
        Integer requestedUnits = Optional.ofNullable(availabilityDto.getNoOfUnits()).orElse(1);
        
        // Calculate already booked vehicles for the period
        Integer bookedVehicles = bookingRepository.findBookedUnitsDuringPeriod(
                transport.getServiceId(),
                availabilityDto.getStartDateTime(),
                availabilityDto.getEndDateTime(),
                BookingStatus.BOOKED
        );

        // Check if enough vehicles are available
        int availableVehicles = transport.getVehicleQty() - bookedVehicles;
        if (availableVehicles < requestedUnits) {
            return createErrorResponse(String.format("Only %d vehicles available, but %d requested", 
                    availableVehicles, requestedUnits));
        }

        // Check passenger capacity
        int totalPassengers = availabilityDto.getAdultCount() + availabilityDto.getChildCount();
        int maxPassengersForRequestedVehicles = transport.getVehicleCapacity() * requestedUnits;
        if (maxPassengersForRequestedVehicles < totalPassengers) {
            return createErrorResponse(String.format("Requested %d vehicles can accommodate max %d passengers, but %d passengers provided", 
                    requestedUnits, maxPassengersForRequestedVehicles, totalPassengers));
        }

        return createSuccessResponse("Transport capacity validation passed");
    }

    private APIResponse<String> validateActivityServiceCapacity(ActivityService activityService, AvailabilityDto availabilityDto) {
        // Check if maxGuests is set (null means unlimited capacity)
        if (activityService.getMaxGuests() == null) {
            return createSuccessResponse("Activity service has unlimited guest capacity");
        }
        
        int totalGuests = availabilityDto.getAdultCount() + availabilityDto.getChildCount();
        if (totalGuests > activityService.getMaxGuests()) {
            return createErrorResponse(String.format("Activity service can accommodate max %d guests, but %d guests provided", 
                    activityService.getMaxGuests(), totalGuests));
        }

        return createSuccessResponse("Activity service capacity validation passed");
    }

    private APIResponse<String> validateFoodAndBeverageCapacity(FoodAndBeverage foodAndBeverage, AvailabilityDto availabilityDto) {
        // Check if maxGuests is set (null means unlimited capacity)
        if (foodAndBeverage.getMaxGuests() == null) {
            return createSuccessResponse("Food and beverage service has unlimited guest capacity");
        }
        
        int totalGuests = availabilityDto.getAdultCount() + availabilityDto.getChildCount();
        if (totalGuests > foodAndBeverage.getMaxGuests()) {
            return createErrorResponse(String.format("Food and beverage service can accommodate max %d guests, but %d guests provided", 
                    foodAndBeverage.getMaxGuests(), totalGuests));
        }

        return createSuccessResponse("Food and beverage capacity validation passed");
    }

    private APIResponse<String> validateTouristGuideCapacity(TouristGuide touristGuide, AvailabilityDto availabilityDto) {
        // Check if maxGuests is set (null means unlimited capacity)
        if (touristGuide.getMaxGuests() == null) {
            return createSuccessResponse("Tourist guide has unlimited guest capacity");
        }
        
        int totalGuests = availabilityDto.getAdultCount() + availabilityDto.getChildCount();
        if (totalGuests > touristGuide.getMaxGuests()) {
            return createErrorResponse(String.format("Tourist guide can accommodate max %d guests, but %d guests provided", 
                    touristGuide.getMaxGuests(), totalGuests));
        }

        return createSuccessResponse("Tourist guide capacity validation passed");
    }

    private APIResponse<AvailabilityResponse> validateTimeSlotBookingDetailed(AvailabilityDto availabilityDto, com.lankatrails.lankatrails_backend.model.Service service) {
        // TIME_SLOTS booking must be for the same day
        if (!availabilityDto.getStartDateTime().toLocalDate().equals(availabilityDto.getEndDateTime().toLocalDate())) {
            return createAvailabilityErrorResponse("Time slot bookings must be for the same day.");
        }

        // Get detailed capacity information
        return validateCapacityDetailed(service, availabilityDto);
    }

    private APIResponse<AvailabilityResponse> validateMultiDayBookingDetailed(AvailabilityDto availabilityDto, com.lankatrails.lankatrails_backend.model.Service service) {
        // Get detailed capacity information
        return validateCapacityDetailed(service, availabilityDto);
    }

    private APIResponse<AvailabilityResponse> validateCapacityDetailed(com.lankatrails.lankatrails_backend.model.Service service, AvailabilityDto availabilityDto) {
        if (service instanceof Accommodation) {
            return validateAccommodationCapacityDetailed((Accommodation) service, availabilityDto);
        } else if (service instanceof Transport) {
            return validateTransportCapacityDetailed((Transport) service, availabilityDto);
        } else if (service instanceof ActivityService) {
            return validateActivityServiceCapacityDetailed((ActivityService) service, availabilityDto);
        } else if (service instanceof FoodAndBeverage) {
            return validateFoodAndBeverageCapacityDetailed((FoodAndBeverage) service, availabilityDto);
        } else if (service instanceof TouristGuide) {
            return validateTouristGuideCapacityDetailed((TouristGuide) service, availabilityDto);
        }
        
        // For other service types, assume unlimited capacity
        AvailabilityResponse response = AvailabilityResponse.builder()
                .available(true)
                .message("Service is available for booking")
                .requestedUnits(Optional.ofNullable(availabilityDto.getNoOfUnits()).orElse(1))
                .build();
        
        return createAvailabilitySuccessResponse(response);
    }

    private APIResponse<AvailabilityResponse> validateAccommodationCapacityDetailed(Accommodation accommodation, AvailabilityDto availabilityDto) {
        // Default to 1 unit if not specified (null-safe)
        Integer requestedUnits = Optional.ofNullable(availabilityDto.getNoOfUnits()).orElse(1);
        
        // Calculate already booked rooms for the period
        Integer bookedRooms = bookingRepository.findBookedUnitsDuringPeriod(
                accommodation.getServiceId(),
                availabilityDto.getStartDateTime(),
                availabilityDto.getEndDateTime(),
                BookingStatus.BOOKED
        );

        int totalRooms = accommodation.getNumberOfRooms();
        int availableRooms = totalRooms - bookedRooms;

        // Check if enough rooms are available
        if (availableRooms < requestedUnits) {
            AvailabilityResponse response = AvailabilityResponse.builder()
                    .available(false)
                    .message(String.format("Only %d rooms available, but %d requested", availableRooms, requestedUnits))
                    .availableUnits(availableRooms)
                    .requestedUnits(requestedUnits)
                    .totalCapacity(totalRooms)
                    .build();
            
            return APIResponse.<AvailabilityResponse>builder()
                    .success(false)
                    .message(response.getMessage())
                    .data(response)
                    .build();
        }

        // Check guest capacity
        int totalGuests = availabilityDto.getAdultCount() + availabilityDto.getChildCount();
        int maxGuestsForRequestedRooms = accommodation.getMaxGuests() * requestedUnits;
        if (maxGuestsForRequestedRooms < totalGuests) {
            AvailabilityResponse response = AvailabilityResponse.builder()
                    .available(false)
                    .message(String.format("Requested %d rooms can accommodate max %d guests, but %d guests provided", 
                            requestedUnits, maxGuestsForRequestedRooms, totalGuests))
                    .availableUnits(availableRooms)
                    .requestedUnits(requestedUnits)
                    .totalCapacity(totalRooms)
                    .build();
            
            return APIResponse.<AvailabilityResponse>builder()
                    .success(false)
                    .message(response.getMessage())
                    .data(response)
                    .build();
        }

        AvailabilityResponse response = AvailabilityResponse.builder()
                .available(true)
                .message("Accommodation is available for booking")
                .availableUnits(availableRooms)
                .requestedUnits(requestedUnits)
                .totalCapacity(totalRooms)
                .build();
        
        return createAvailabilitySuccessResponse(response);
    }

    private APIResponse<AvailabilityResponse> validateTransportCapacityDetailed(Transport transport, AvailabilityDto availabilityDto) {
        // Default to 1 unit if not specified (null-safe)
        Integer requestedUnits = Optional.ofNullable(availabilityDto.getNoOfUnits()).orElse(1);
        
        // Calculate already booked vehicles for the period
        Integer bookedVehicles = bookingRepository.findBookedUnitsDuringPeriod(
                transport.getServiceId(),
                availabilityDto.getStartDateTime(),
                availabilityDto.getEndDateTime(),
                BookingStatus.BOOKED
        );

        int totalVehicles = transport.getVehicleQty();
        int availableVehicles = totalVehicles - bookedVehicles;

        // Check if enough vehicles are available
        if (availableVehicles < requestedUnits) {
            AvailabilityResponse response = AvailabilityResponse.builder()
                    .available(false)
                    .message(String.format("Only %d vehicles available, but %d requested", availableVehicles, requestedUnits))
                    .availableUnits(availableVehicles)
                    .requestedUnits(requestedUnits)
                    .totalCapacity(totalVehicles)
                    .build();
            
            return APIResponse.<AvailabilityResponse>builder()
                    .success(false)
                    .message(response.getMessage())
                    .data(response)
                    .build();
        }

        // Check passenger capacity
        int totalPassengers = availabilityDto.getAdultCount() + availabilityDto.getChildCount();
        int maxPassengersForRequestedVehicles = transport.getVehicleCapacity() * requestedUnits;
        if (maxPassengersForRequestedVehicles < totalPassengers) {
            AvailabilityResponse response = AvailabilityResponse.builder()
                    .available(false)
                    .message(String.format("Requested %d vehicles can accommodate max %d passengers, but %d passengers provided", 
                            requestedUnits, maxPassengersForRequestedVehicles, totalPassengers))
                    .availableUnits(availableVehicles)
                    .requestedUnits(requestedUnits)
                    .totalCapacity(totalVehicles)
                    .build();
            
            return APIResponse.<AvailabilityResponse>builder()
                    .success(false)
                    .message(response.getMessage())
                    .data(response)
                    .build();
        }

        AvailabilityResponse response = AvailabilityResponse.builder()
                .available(true)
                .message("Transport is available for booking")
                .availableUnits(availableVehicles)
                .requestedUnits(requestedUnits)
                .totalCapacity(totalVehicles)
                .build();
        
        return createAvailabilitySuccessResponse(response);
    }

    private APIResponse<AvailabilityResponse> validateActivityServiceCapacityDetailed(ActivityService activityService, AvailabilityDto availabilityDto) {
        int totalGuests = availabilityDto.getAdultCount() + availabilityDto.getChildCount();
        
        // Check if maxGuests is set (null means unlimited capacity)
        if (activityService.getMaxGuests() == null) {
            AvailabilityResponse response = AvailabilityResponse.builder()
                    .available(true)
                    .message("Activity service has unlimited guest capacity and is available for booking")
                    .requestedUnits(Optional.ofNullable(availabilityDto.getNoOfUnits()).orElse(1))
                    .build();
            
            return createAvailabilitySuccessResponse(response);
        }

        // Check guest capacity
        if (totalGuests > activityService.getMaxGuests()) {
            AvailabilityResponse response = AvailabilityResponse.builder()
                    .available(false)
                    .message(String.format("Activity service can accommodate max %d guests, but %d guests provided", 
                            activityService.getMaxGuests(), totalGuests))
                    .totalCapacity(activityService.getMaxGuests())
                    .requestedUnits(Optional.ofNullable(availabilityDto.getNoOfUnits()).orElse(1))
                    .build();
            
            return APIResponse.<AvailabilityResponse>builder()
                    .success(false)
                    .message(response.getMessage())
                    .data(response)
                    .build();
        }

        AvailabilityResponse response = AvailabilityResponse.builder()
                .available(true)
                .message("Activity service capacity validation passed")
                .totalCapacity(activityService.getMaxGuests())
                .requestedUnits(Optional.ofNullable(availabilityDto.getNoOfUnits()).orElse(1))
                .build();
        
        return createAvailabilitySuccessResponse(response);
    }

    private APIResponse<AvailabilityResponse> validateFoodAndBeverageCapacityDetailed(FoodAndBeverage foodAndBeverage, AvailabilityDto availabilityDto) {
        int totalGuests = availabilityDto.getAdultCount() + availabilityDto.getChildCount();
        
        // Check if maxGuests is set (null means unlimited capacity)
        if (foodAndBeverage.getMaxGuests() == null) {
            AvailabilityResponse response = AvailabilityResponse.builder()
                    .available(true)
                    .message("Food and beverage service has unlimited guest capacity and is available for booking")
                    .requestedUnits(Optional.ofNullable(availabilityDto.getNoOfUnits()).orElse(1))
                    .build();
            
            return createAvailabilitySuccessResponse(response);
        }

        // Check guest capacity
        if (totalGuests > foodAndBeverage.getMaxGuests()) {
            AvailabilityResponse response = AvailabilityResponse.builder()
                    .available(false)
                    .message(String.format("Food and beverage service can accommodate max %d guests, but %d guests provided", 
                            foodAndBeverage.getMaxGuests(), totalGuests))
                    .totalCapacity(foodAndBeverage.getMaxGuests())
                    .requestedUnits(Optional.ofNullable(availabilityDto.getNoOfUnits()).orElse(1))
                    .build();
            
            return APIResponse.<AvailabilityResponse>builder()
                    .success(false)
                    .message(response.getMessage())
                    .data(response)
                    .build();
        }

        AvailabilityResponse response = AvailabilityResponse.builder()
                .available(true)
                .message("Food and beverage capacity validation passed")
                .totalCapacity(foodAndBeverage.getMaxGuests())
                .requestedUnits(Optional.ofNullable(availabilityDto.getNoOfUnits()).orElse(1))
                .build();
        
        return createAvailabilitySuccessResponse(response);
    }

    private APIResponse<AvailabilityResponse> validateTouristGuideCapacityDetailed(TouristGuide touristGuide, AvailabilityDto availabilityDto) {
        int totalGuests = availabilityDto.getAdultCount() + availabilityDto.getChildCount();
        
        // Check if maxGuests is set (null means unlimited capacity)
        if (touristGuide.getMaxGuests() == null) {
            AvailabilityResponse response = AvailabilityResponse.builder()
                    .available(true)
                    .message("Tourist guide has unlimited guest capacity and is available for booking")
                    .requestedUnits(Optional.ofNullable(availabilityDto.getNoOfUnits()).orElse(1))
                    .build();
            
            return createAvailabilitySuccessResponse(response);
        }

        // Check guest capacity
        if (totalGuests > touristGuide.getMaxGuests()) {
            AvailabilityResponse response = AvailabilityResponse.builder()
                    .available(false)
                    .message(String.format("Tourist guide can accommodate max %d guests, but %d guests provided", 
                            touristGuide.getMaxGuests(), totalGuests))
                    .totalCapacity(touristGuide.getMaxGuests())
                    .requestedUnits(Optional.ofNullable(availabilityDto.getNoOfUnits()).orElse(1))
                    .build();
            
            return APIResponse.<AvailabilityResponse>builder()
                    .success(false)
                    .message(response.getMessage())
                    .data(response)
                    .build();
        }

        AvailabilityResponse response = AvailabilityResponse.builder()
                .available(true)
                .message("Tourist guide capacity validation passed")
                .totalCapacity(touristGuide.getMaxGuests())
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
