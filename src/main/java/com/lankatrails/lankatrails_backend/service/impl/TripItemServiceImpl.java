package com.lankatrails.lankatrails_backend.service.impl;

import com.lankatrails.lankatrails_backend.dtos.AvailabilityDto;
import com.lankatrails.lankatrails_backend.dtos.request.TripItemDTO;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.AvailabilityResponse;
import com.lankatrails.lankatrails_backend.exception.IllegalParamsException;
import com.lankatrails.lankatrails_backend.exception.ResourceNotFoundException;
import com.lankatrails.lankatrails_backend.model.Place;
import com.lankatrails.lankatrails_backend.model.Service;
import com.lankatrails.lankatrails_backend.model.Trip;
import com.lankatrails.lankatrails_backend.model.TripItem;
import com.lankatrails.lankatrails_backend.repositories.PlaceRepository;
import com.lankatrails.lankatrails_backend.repositories.ServiceRepository;
import com.lankatrails.lankatrails_backend.repositories.TripItemRepository;
import com.lankatrails.lankatrails_backend.repositories.TripRepository;
import com.lankatrails.lankatrails_backend.service.BookingService;
import com.lankatrails.lankatrails_backend.service.TripItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
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

        boolean hasOverlap = tripItemRepository.existsOverlappingTripItemsForTripId(
                tripId, startDateTime, endDateTime);

        String message = hasOverlap ? "Overlapping trip items found" : "No overlapping trip items";

        log.info(message);
        return hasOverlap;
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
