package com.lankatrails.lankatrails_backend.service.impl;

import com.lankatrails.lankatrails_backend.dtos.ChatRoomDto;
import com.lankatrails.lankatrails_backend.dtos.request.TripItemDTO;
import com.lankatrails.lankatrails_backend.dtos.request.TripRequestDTO;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.TripResponseDTO;
import com.lankatrails.lankatrails_backend.exception.ResourceNotFoundException;
import com.lankatrails.lankatrails_backend.exception.UserNotFoundException;
import com.lankatrails.lankatrails_backend.model.*;
import com.lankatrails.lankatrails_backend.model.enums.BudgetCategory;
import com.lankatrails.lankatrails_backend.model.enums.TripStatus;
import com.lankatrails.lankatrails_backend.repositories.TouristRepository;
import com.lankatrails.lankatrails_backend.repositories.TripRepository;
import com.lankatrails.lankatrails_backend.security.utils.AuthUtils;
import com.lankatrails.lankatrails_backend.service.ChatRoomService;
import com.lankatrails.lankatrails_backend.service.TripService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class TripServiceImpl implements TripService {
    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private TouristRepository touristRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private AuthUtils authUtils;

    @Autowired
    private ChatRoomService chatRoomService;

    @Override
    @Transactional
    public APIResponse<TripResponseDTO> createTrip(TripRequestDTO tripRequestDTO) {
        log.info("Creating trip with request: {}", tripRequestDTO);
        // Validate dates
        if (tripRequestDTO.getStartDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Start date must be today or in the future");
        }
        if (tripRequestDTO.getEndDate().isBefore(tripRequestDTO.getStartDate())) {
            throw new IllegalArgumentException("End date must be after start date");
        }

        // Validate positive values
        if (tripRequestDTO.getNumberOfAdults() != null && tripRequestDTO.getNumberOfAdults() <= 0) {
            throw new IllegalArgumentException("Number of adults must be positive");
        }

        // Convert DTO to entity
        Trip trip = modelMapper.map(tripRequestDTO, Trip.class);

        // Set additional properties
        trip.setTripStatus(TripStatus.PLANNING);

        // Initialize collections
        trip.setTourists(new HashSet<>());
        trip.setLocations(new HashSet<>());
        trip.setTripItems(new ArrayList<>());
        trip.setTripExpenses(new ArrayList<>());
        trip.setTripBudgetCategoryLimits(initializeCategoryLimits(tripRequestDTO, trip));

        // Get and set logged-in user
        User currentUser = touristRepository.findById(authUtils.loggedInUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + authUtils.loggedInUserId()));
        if (!(currentUser instanceof Tourist leadTourist)) {
            throw new IllegalStateException("Only tourists can create trips");
        }

        trip.setLeadTourist(leadTourist);
        trip.getTourists().add(leadTourist);

        //add start location to locations
//        trip.getLocations().add(modelMapper.map(tripRequestDTO.getStartLocation(), Location.class));

        // Set default values if not provided
        if (trip.getNumberOfAdults() == null) {
            trip.setNumberOfAdults(1);
        }
        if (trip.getNumberOfChildren() == null) {
            trip.setNumberOfChildren(0);
        }
        if (trip.getTotalBudget() == null) {
            trip.setTotalBudget(0.0);
        }

        // Save the trip
        Trip savedTrip = tripRepository.save(trip);

        // Convert to response DTO
        TripResponseDTO responseDTO = modelMapper.map(savedTrip, TripResponseDTO.class);

        return new APIResponse<>(true, "Trip created successfully", responseDTO);
    }

    @Override
    @Transactional
    public APIResponse<List<TripResponseDTO>> getAllMyTrips() {
        log.info("Fetching all trips for the logged-in user");
        User currentUser = touristRepository.findById(authUtils.loggedInUserId())
                .orElseThrow(() -> new UserNotFoundException("Tourist not found with id: " + authUtils.loggedInUserId()));
        if (!(currentUser instanceof Tourist tourist)) {
            throw new IllegalStateException("Only tourists can fetch their trips");
        }

        List<Trip> trips = tripRepository.findByTouristsContaining(tourist);
        if (trips.isEmpty()) {
            return new APIResponse<>(false, "No trips found for the user", new ArrayList<>());
        }

        List<TripResponseDTO> responseDTOs = new ArrayList<>();
        for (Trip trip : trips) {
            responseDTOs.add(modelMapper.map(trip, TripResponseDTO.class));
        }

        return new APIResponse<>(true, "Trips fetched successfully", responseDTOs);
    }

    @Override
    @Transactional
    public APIResponse<TripResponseDTO> getTripById(Long tripId) {
        log.info("Fetching trip with ID: {}", tripId);
        Trip trip = tripRepository.findByTripId(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip", tripId));

        TripResponseDTO responseDTO = modelMapper.map(trip, TripResponseDTO.class);
        return new APIResponse<>(true, "Trip fetched successfully", responseDTO);
    }

    @Override
    @Transactional
    public APIResponse<List<TripItemDTO>> getTripItemsByTripId(Long tripId) {
        log.info("Fetching trip items for trip with ID: {}", tripId);
        Trip trip = tripRepository.findByTripId(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip", tripId));

        List<TripItemDTO> tripItemDTOs = new ArrayList<>();
        for (TripItem item : trip.getTripItems()) {
            tripItemDTOs.add(modelMapper.map(item, TripItemDTO.class));
        }

        return new APIResponse<>(true, "Trip items fetched successfully", tripItemDTOs);
    }

    @Override
    @Transactional
    public APIResponse<String> addTouristToTrip(Long tripId, Long touristId) {
        log.info("Adding tourist with ID: {} to trip with ID: {}", touristId, tripId);
        Trip trip = tripRepository.findByTripId(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip", tripId));

        Tourist tourist = touristRepository.findById(touristId)
                .orElseThrow(() -> new UserNotFoundException("Tourist not found with id: " + touristId));

        // Check if the tourist is already part of the trip
        if (trip.getTourists().contains(tourist)) {
            return new APIResponse<>(false, "Tourist is already part of this trip", null);
        }

        // Add the tourist to the trip
        trip.getTourists().add(tourist);
        tripRepository.save(trip);

        // Create or update the chat room for the trip
        chatRoomService.setChatRoomForTrip(trip);

        return new APIResponse<>(true, "Tourist added to trip successfully", null);
    }

    @Override
    @Transactional
    public APIResponse<String> removeTouristFromTrip(Long tripId, Long touristId) {
        log.info("Removing tourist with ID: {} from trip with ID: {}", touristId, tripId);

        Trip trip = tripRepository.findByTripId(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip", tripId));

        if (!trip.getLeadTourist().getUserId().equals(authUtils.loggedInUserId())) {
            return new APIResponse<>(false, "Only the lead tourist can remove tourists from the trip", null);
        }

        // Validate tourist lead tourist
        if (trip.getLeadTourist().getUserId().equals(touristId)) {
            return new APIResponse<>(false, "Cannot remove the lead tourist from the trip", null);
        }

        Tourist tourist = touristRepository.findById(touristId)
                .orElseThrow(() -> new UserNotFoundException("Tourist not found with id: " + touristId));

        // Check if the tourist is part of the trip
        if (!trip.getTourists().contains(tourist)) {
            return new APIResponse<>(false, "Tourist is not part of this trip", null);
        }

        // Remove the tourist from the trip
        trip.getTourists().remove(tourist);
        tripRepository.save(trip);

        // Update the chat room for the trip
        chatRoomService.setChatRoomForTrip(trip);

        return new APIResponse<>(true, "Tourist removed from trip successfully", null);
    }

    private Set<TripBudgetCategoryLimit> initializeCategoryLimits(TripRequestDTO tripRequestDTO, Trip trip) {
        Set<TripBudgetCategoryLimit> categoryLimits = new HashSet<>();

        if (tripRequestDTO.getAccommodationLimit() != null) {
            categoryLimits.add(new TripBudgetCategoryLimit(
                    BudgetCategory.ACCOMMODATION, tripRequestDTO.getAccommodationLimit(), trip));
        }
        if (tripRequestDTO.getFoodLimit() != null) {
            categoryLimits.add(new TripBudgetCategoryLimit(
                    BudgetCategory.FOOD, tripRequestDTO.getFoodLimit(), trip));
        }
        if (tripRequestDTO.getTransportLimit() != null) {
            categoryLimits.add(new TripBudgetCategoryLimit(
                    BudgetCategory.TRANSPORT, tripRequestDTO.getTransportLimit(), trip));
        }
        if (tripRequestDTO.getActivityLimit() != null) {
            categoryLimits.add(new TripBudgetCategoryLimit(
                    BudgetCategory.ACTIVITY, tripRequestDTO.getActivityLimit(), trip));
        }
        if (tripRequestDTO.getMiscellaneousLimit() != null) {
            categoryLimits.add(new TripBudgetCategoryLimit(
                    BudgetCategory.MISCELLANEOUS, tripRequestDTO.getMiscellaneousLimit(), trip));
        }
        if (tripRequestDTO.getShoppingLimit() != null) {
            categoryLimits.add(new TripBudgetCategoryLimit(
                    BudgetCategory.SHOPPING, tripRequestDTO.getTotalBudgetLimit(), trip));
        }

        return categoryLimits;
    }
}
