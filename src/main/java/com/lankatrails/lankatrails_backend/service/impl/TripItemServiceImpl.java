package com.lankatrails.lankatrails_backend.service.impl;

import com.lankatrails.lankatrails_backend.dtos.AvailabilityDto;
import com.lankatrails.lankatrails_backend.dtos.request.LocationDTO;
import com.lankatrails.lankatrails_backend.dtos.request.TripItemDTO;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.AvailabilityResponse;
import com.lankatrails.lankatrails_backend.exception.IllegalParamsException;
import com.lankatrails.lankatrails_backend.exception.ResourceNotFoundException;
import com.lankatrails.lankatrails_backend.model.*;
import com.lankatrails.lankatrails_backend.model.enums.TransportMode;
import com.lankatrails.lankatrails_backend.repositories.PlaceRepository;
import com.lankatrails.lankatrails_backend.repositories.ServiceRepository;
import com.lankatrails.lankatrails_backend.repositories.TripItemRepository;
import com.lankatrails.lankatrails_backend.repositories.TripRepository;
import com.lankatrails.lankatrails_backend.service.BookingService;
import com.lankatrails.lankatrails_backend.service.TravelTimeService;
import com.lankatrails.lankatrails_backend.service.TripItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class TripItemServiceImpl implements TripItemService {
    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private TripItemRepository tripItemRepository;

    @Autowired
    private PlaceRepository placeRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private TravelTimeService travelTimeService;

    @Override
    public APIResponse<String> addTripItem(Long tripId, TripItemDTO tripItemDTO) {
        log.info("Adding trip item to trip with ID: {}", tripId);

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("TripId", tripId));

        TripItem tripItem = modelMapper.map(tripItemDTO, TripItem.class);
        tripItem.setTrip(trip);

        // Check for trip date constraints
        if (tripItem.getStartTime().toLocalDate().isBefore(trip.getStartDate()) ||
                tripItem.getEndTime().toLocalDate().isAfter(trip.getEndDate())) {
            return createErrorResponse("Trip item dates must be within the trip date range");
        }

        // Check for overlapping trip items
        if (hasOverlappingTripItems(tripId, tripItemDTO.getStartTime(), tripItemDTO.getEndTime())) {
            return createErrorResponse("Trip item time overlaps with existing trip items");
        }

        // Check travel time feasibility with previous trip item
        APIResponse<String> travelTimeCheck = checkTravelTimeFeasibility(tripItemDTO, trip);
        if (!travelTimeCheck.isSuccess()) {
            return travelTimeCheck;
        }

        switch (tripItemDTO.getType()) {
            case PLACE -> {
                if (tripItemDTO.getPlace() == null || tripItemDTO.getPlace().getPlaceId() == null) {
                    throw new IllegalParamsException("Place must be provided with a valid ID");
                }

                Place place;
                String placeId = tripItemDTO.getPlace().getPlaceId();

                // Check if place already exists
                place = placeRepository.findById(placeId).orElseGet(() -> {
                    // Save if not exist
                    return placeRepository.save(modelMapper.map(tripItemDTO.getPlace(), Place.class));
                });

                tripItem.setPlace(place);
            }

            case SERVICE -> {
                if (tripItemDTO.getService().getServiceId() == null) {
                    throw new IllegalParamsException("Service ID must be provided");
                }
                // Fetch the service by ID
                Service service = serviceRepository.findById(tripItemDTO.getService().getServiceId())
                        .orElseThrow(() -> new IllegalParamsException("Service not found with ID: " + tripItemDTO.getService().getServiceId()));

                // Default to 1 unit if not specified (null-safe)
                Integer requestedUnits = Optional.ofNullable(tripItemDTO.getNoOfUnits()).orElse(1);
                if (requestedUnits <= 0) {
                    return createErrorResponse("Number of units must be at least 1");
                }

                AvailabilityDto availabilityDto = AvailabilityDto.builder()
                        .startDateTime(tripItemDTO.getStartTime())
                        .endDateTime(tripItemDTO.getEndTime())
                        .adultCount(tripItemDTO.getNumberOfAdults())
                        .childCount(tripItemDTO.getNumberOfChildren())
                        .serviceId(service.getServiceId())
                        .tripId(tripId)
                        .noOfUnits(requestedUnits)
                        .build();

                // Check availability
                APIResponse<AvailabilityResponse> availabilityResponse = bookingService.checkAvailability(availabilityDto);
                if (!availabilityResponse.isSuccess() || !availabilityResponse.getData().isAvailable()) {
                    return createErrorResponse(availabilityResponse.getMessage());
                }

                // Set the service and number of units on the trip item
                tripItem.setService(service);
                tripItem.setNoOfUnits(requestedUnits);
            }

            default -> throw new IllegalParamsException("Invalid trip item type: " + tripItemDTO.getType());
        }

        // Save the trip item
        tripItemRepository.save(tripItem);

        return createSuccessResponse("Trip item added successfully");
    }

    @Override
    public Boolean hasOverlappingTripItems(Long tripId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        log.info("Checking for overlapping trip items in trip with ID: {}", tripId);

        if (startDateTime.isAfter(endDateTime) || startDateTime.isEqual(endDateTime)) {
            throw new IllegalParamsException("Start date-time must be before end date-time");
        }

        // Get all existing trip items that might overlap
        List<TripItem> overlappingItems = tripItemRepository.findOverlappingTripItemsForTripId(
                tripId, startDateTime, endDateTime);

        if (overlappingItems.isEmpty()) {
            log.info("No overlapping trip items found");
            return false;
        }

        // Check each overlapping item for type-specific rules
        for (TripItem existingItem : overlappingItems) {
            // If existing item is a multi-day accommodation, allow new items to fall within it
            if (isMultiDayAccommodation(existingItem)) {
                // Check if the new item falls completely within the accommodation period
                if (startDateTime.isAfter(existingItem.getStartTime()) &&
                        endDateTime.isBefore(existingItem.getEndTime())) {
                    log.info("New trip item falls within multi-day accommodation period - allowing");
                    continue; // This overlap is allowed
                }
                // If new item extends beyond accommodation, it's a conflict
                log.info("Trip item conflicts with accommodation boundaries");
                return true;
            }

            // For non-accommodation items, any overlap is a conflict
            log.info("Overlapping trip items found with non-accommodation item");
            return true;
        }

        log.info("No conflicting trip items found");
        return false;
    }

    /**
     * Checks if a trip item is a multi-day accommodation
     */
    private boolean isMultiDayAccommodation(TripItem tripItem) {
        if (tripItem.getService() == null || tripItem.getService().getBookingConfiguration() == null) {
            return false;
        }

        // Check if it's a multi-day service (accommodation)
        return tripItem.getService().getBookingConfiguration().getBookingType() ==
                com.lankatrails.lankatrails_backend.model.enums.BookingType.MULTI_DAY;
    }

    private APIResponse<String> checkTravelTimeFeasibility(TripItemDTO tripItemDTO, Trip trip) {
        // Check travel time feasibility with previous trip item
        Optional<TripItem> previousTripItemOpt = tripItemRepository
                .findTopByTripAndEndTimeLessThanEqualOrderByEndTimeDesc(trip, tripItemDTO.getStartTime());

        if (previousTripItemOpt.isPresent()) {
            TripItem previousTripItem = previousTripItemOpt.get();

            // Get coordinates for previous location
            Optional<Double[]> previousCoordinates = getLocationCoordinates(previousTripItem);

            // Get coordinates for current location
            Optional<Double[]> currentCoordinates = getLocationCoordinates(tripItemDTO);

            // If both locations have coordinates, validate travel time
            if (previousCoordinates.isPresent() && currentCoordinates.isPresent()) {
                APIResponse<String> previousCheck = validateTravelTime(
                        previousCoordinates.get(),
                        currentCoordinates.get(),
                        previousTripItem.getEndTime(),
                        tripItemDTO.getStartTime()
                );
                if (!previousCheck.isSuccess()) {
                    return previousCheck;
                }
            }
        }

        // Check travel time feasibility with next trip item
        Optional<TripItem> nextTripItemOpt = tripItemRepository
                .findTopByTripAndStartTimeGreaterThanEqualOrderByStartTimeAsc(trip, tripItemDTO.getEndTime());

        if (nextTripItemOpt.isPresent()) {
            TripItem nextTripItem = nextTripItemOpt.get();

            // Get coordinates for current location
            Optional<Double[]> currentCoordinates = getLocationCoordinates(tripItemDTO);

            // Get coordinates for next location
            Optional<Double[]> nextCoordinates = getLocationCoordinates(nextTripItem);

            // If both locations have coordinates, validate travel time
            if (currentCoordinates.isPresent() && nextCoordinates.isPresent()) {
                APIResponse<String> nextCheck = validateTravelTime(
                        currentCoordinates.get(),
                        nextCoordinates.get(),
                        tripItemDTO.getEndTime(),
                        nextTripItem.getStartTime()
                );
                if (!nextCheck.isSuccess()) {
                    return nextCheck;
                }
            }
        }

        return createSuccessResponse("Travel time feasibility check passed");
    }

    /**
     * Extract coordinates from a TripItem (either from Place or Service)
     */
    private Optional<Double[]> getLocationCoordinates(TripItem tripItem) {
        if (tripItem.getPlace() != null) {
            return Optional.of(new Double[]{
                    tripItem.getPlace().getLatitude(),
                    tripItem.getPlace().getLongitude()
            });
        }

        if (tripItem.getService() != null) {
            Location serviceLocation = tripItem.getService().getLocations().stream().findFirst()
                    .orElseThrow(() -> new IllegalParamsException("Service has no associated locations"));
            return Optional.of(new Double[]{
                    serviceLocation.getLatitude(),
                    serviceLocation.getLongitude()
            });
        }

        return Optional.empty();
    }

    /**
     * Extract coordinates from a TripItemDTO (either from Place or Service)
     */
    private Optional<Double[]> getLocationCoordinates(TripItemDTO tripItemDTO) {
        if (tripItemDTO.getPlace() != null) {
            return Optional.of(new Double[]{
                    tripItemDTO.getPlace().getLatitude(),
                    tripItemDTO.getPlace().getLongitude()
            });
        }

        if (tripItemDTO.getService() != null) {
            LocationDTO serviceLocation = tripItemDTO.getService().getLocations().stream().findFirst()
                    .orElseThrow(() -> new IllegalParamsException("Service has no associated locations"));
            return Optional.of(new Double[]{
                    serviceLocation.getLatitude(),
                    serviceLocation.getLongitude()
            });
        }

        return Optional.empty();
    }

    /**
     * Validate if there's sufficient travel time between two locations
     */
    private APIResponse<String> validateTravelTime(Double[] fromCoordinates, Double[] toCoordinates,
                                                   LocalDateTime endTime, LocalDateTime startTime) {
        Duration travelDuration = travelTimeService.calculateTravelTime(
                fromCoordinates[0], // latitude
                fromCoordinates[1], // longitude
                toCoordinates[0],   // latitude
                toCoordinates[1],   // longitude
                TransportMode.DRIVING // Assuming driving mode; could be parameterized
        );

        Duration availableDuration = Duration.between(endTime, startTime);

        if (travelDuration.compareTo(availableDuration) > 0) {
            return createErrorResponse("Insufficient travel time between previous trip item and this one");
        }

        return createSuccessResponse("Travel time validation passed");
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
}
