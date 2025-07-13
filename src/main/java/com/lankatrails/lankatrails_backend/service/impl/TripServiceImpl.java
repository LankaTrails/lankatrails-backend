package com.lankatrails.lankatrails_backend.service.impl;

import com.lankatrails.lankatrails_backend.dtos.request.TripRequestDTO;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.TripResponseDTO;
import com.lankatrails.lankatrails_backend.model.Tourist;
import com.lankatrails.lankatrails_backend.model.Trip;
import com.lankatrails.lankatrails_backend.model.TripBudgetCategoryLimit;
import com.lankatrails.lankatrails_backend.model.User;
import com.lankatrails.lankatrails_backend.model.enums.BudgetCategory;
import com.lankatrails.lankatrails_backend.repositories.TripRepository;
import com.lankatrails.lankatrails_backend.security.utils.AuthUtils;
import com.lankatrails.lankatrails_backend.service.TripService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    private ModelMapper modelMapper;

    @Autowired
    private AuthUtils authUtils;

    @Override
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
        if (tripRequestDTO.getNumberOfPeople() != null && tripRequestDTO.getNumberOfPeople() <= 0) {
            throw new IllegalArgumentException("Number of people must be positive");
        }

        // Convert DTO to entity
        Trip trip = modelMapper.map(tripRequestDTO, Trip.class);

        // Initialize collections
        trip.setTourists(new HashSet<>());
        trip.setTripItems(new ArrayList<>());
        trip.setTripExpenses(new ArrayList<>());
        trip.setTripBudgetCategoryLimits(initializeCategoryLimits(tripRequestDTO, trip));

        // Get and set logged-in user
        User currentUser = authUtils.loggedInUser();
        if (!(currentUser instanceof Tourist leadTourist)) {
            throw new IllegalStateException("Only tourists can create trips");
        }

        trip.setLeadTourist(leadTourist);
        trip.getTourists().add(leadTourist);

        // Set default values if not provided
        if (trip.getNumberOfPeople() == null) {
            trip.setNumberOfPeople(1);
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
    public APIResponse<List<TripResponseDTO>> getAllMyTrips() {
        log.info("Fetching all trips for the logged-in user");
        User currentUser = authUtils.loggedInUser();
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
    public APIResponse<TripResponseDTO> getTripById(Long tripId) {
        log.info("Fetching trip with ID: {}", tripId);
        Trip trip = tripRepository.findByTripId(tripId);
        if (trip == null) {
            return new APIResponse<>(false, "Trip not found", null);
        }

        TripResponseDTO responseDTO = modelMapper.map(trip, TripResponseDTO.class);
        return new APIResponse<>(true, "Trip fetched successfully", responseDTO);
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
