package com.lankatrails.lankatrails_backend.service.impl;

import com.lankatrails.lankatrails_backend.dtos.ChatRoomDto;
import com.lankatrails.lankatrails_backend.dtos.TripParticipantDto;
import com.lankatrails.lankatrails_backend.dtos.TripPeriodDto;
import com.lankatrails.lankatrails_backend.dtos.request.LocationDTO;
import com.lankatrails.lankatrails_backend.dtos.request.TripItemDTO;
import com.lankatrails.lankatrails_backend.dtos.request.TripRequestDTO;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.TripResponseDTO;
import com.lankatrails.lankatrails_backend.exception.BadRequestException;
import com.lankatrails.lankatrails_backend.exception.ResourceNotFoundException;
import com.lankatrails.lankatrails_backend.exception.UserNotFoundException;
import com.lankatrails.lankatrails_backend.model.*;
import com.lankatrails.lankatrails_backend.model.enums.BudgetCategory;
import com.lankatrails.lankatrails_backend.model.enums.TripPrivilege;
import com.lankatrails.lankatrails_backend.model.enums.TripRole;
import com.lankatrails.lankatrails_backend.model.enums.TripStatus;
import com.lankatrails.lankatrails_backend.repositories.*;
import com.lankatrails.lankatrails_backend.security.utils.AuthUtils;
import com.lankatrails.lankatrails_backend.service.ChatRoomService;
import com.lankatrails.lankatrails_backend.service.TripService;
import com.lankatrails.lankatrails_backend.service.utils.TripPrivilegeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
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

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private TripItemRepository tripItemRepository;

    @Autowired
    private TripPrivilegeUtils tripPrivilegeUtils;

    @Override
    @Transactional
    public APIResponse<TripResponseDTO> createTrip(TripRequestDTO tripRequestDTO) {
        log.info("Creating trip with request: {}", tripRequestDTO);
        // Get and set logged-in user
        User currentUser = touristRepository.findById(authUtils.loggedInUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + authUtils.loggedInUserId()));
        if (!(currentUser instanceof Tourist leadTourist)) {
            throw new IllegalStateException("Only tourists can create trips");
        }

        // Validate existing trips in the same period
         List<Trip> existingTrips = tripRepository.findOverlappingTripsForTourist(leadTourist, tripRequestDTO.getStartDate(), tripRequestDTO.getEndDate());
        if (!existingTrips.isEmpty()) {
            throw new BadRequestException("You already have a trip scheduled during this period");
        }

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

        // Validate budget limit
        if (tripRequestDTO.getTotalBudgetLimit() != null && tripRequestDTO.getTotalBudgetLimit().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Budget limit cannot be negative");
        }

        // Convert DTO to entity
        Trip trip = modelMapper.map(tripRequestDTO, Trip.class);

        // Set additional properties
        trip.setTripStatus(TripStatus.PLANNING);

        // Initialize collections
        trip.setParticipants(new HashSet<>());
        trip.setLocations(new ArrayList<>());
        trip.setTripItems(new ArrayList<>());
        trip.setTripExpenses(new ArrayList<>());
        trip.setTripBudgetCategories(new HashSet<>());

        // Set lead tourist as admin participant
        TripParticipant leadParticipant = TripParticipant.builder()
                .tourist(leadTourist)
                .tripRole(TripRole.ADMIN)
                .joinedAt(LocalDateTime.now())
                .trip(trip)
                .build();

        leadParticipant.setPrivileges(tripPrivilegeUtils.getDefaultPrivileges(TripRole.ADMIN));
        trip.getParticipants().add(leadParticipant);

        // Set locations for the trip
        List<Location> locations = setLocationsForTrip(tripRequestDTO.getLocations(), tripRequestDTO.getStartLocation());
        trip.setLocations(locations);
        trip.setStartLocation(locations.getFirst());

        // Set default values if not provided
        if (trip.getNumberOfAdults() == null) {
            trip.setNumberOfAdults(1);
        }
        if (trip.getNumberOfChildren() == null) {
            trip.setNumberOfChildren(0);
        }
        if (trip.getTotalBudget() == null) {
            trip.setTotalBudget(BigDecimal.ZERO);
        }

        // Save the trip
        Trip savedTrip = tripRepository.save(trip);

        // Convert to response DTO
        TripResponseDTO responseDTO = modelMapper.map(savedTrip, TripResponseDTO.class);

        return new APIResponse<>(true, "Trip created successfully", responseDTO);
    }

    @Override
    @Transactional
    public APIResponse<TripResponseDTO> editTrip(Long tripId, TripRequestDTO tripRequestDTO) {
        log.info("Editing trip with ID: {} and request: {}", tripId, tripRequestDTO);
        
        // Get the current user
        User currentUser = touristRepository.findById(authUtils.loggedInUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + authUtils.loggedInUserId()));
        if (!(currentUser instanceof Tourist tourist)) {
            throw new IllegalStateException("Only tourists can edit trips");
        }

        // Find the existing trip
        Trip existingTrip = tripRepository.findByTripId(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip", tripId));

        // Check if the user has permission to edit this trip (must be a participant with admin or editor role)
        TripParticipant userParticipant = existingTrip.getParticipants().stream()
                .filter(participant -> participant.getTourist().getUserId().equals(authUtils.loggedInUserId()))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("You are not a participant of this trip"));

        // Check if user has edit privileges
        if (!tripPrivilegeUtils.hasPrivilege(userParticipant.getTripRole(), TripPrivilege.EDIT_TRIP_DETAILS)) {
            throw new BadRequestException("You don't have permission to edit this trip");
        }

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
        if (tripRequestDTO.getNumberOfChildren() != null && tripRequestDTO.getNumberOfChildren() < 0) {
            throw new IllegalArgumentException("Number of children cannot be negative");
        }

        // Check for overlapping trips (excluding the current trip)
        List<Trip> overlappingTrips = tripRepository.findOverlappingTripsForTourist(tourist, tripRequestDTO.getStartDate(), tripRequestDTO.getEndDate());
        overlappingTrips.removeIf(trip -> trip.getTripId().equals(tripId)); // Exclude current trip
        if (!overlappingTrips.isEmpty()) {
            throw new BadRequestException("You already have another trip scheduled during this period");
        }

        // Validate trip dates against existing trip items
        validateTripDatesAgainstTripItems(tripId, tripRequestDTO.getStartDate(), tripRequestDTO.getEndDate());

        // Update trip fields
        existingTrip.setTripName(tripRequestDTO.getTripName());
        existingTrip.setStartDate(tripRequestDTO.getStartDate());
        existingTrip.setEndDate(tripRequestDTO.getEndDate());
        existingTrip.setNumberOfAdults(tripRequestDTO.getNumberOfAdults() != null ? tripRequestDTO.getNumberOfAdults() : existingTrip.getNumberOfAdults());
        existingTrip.setNumberOfChildren(tripRequestDTO.getNumberOfChildren() != null ? tripRequestDTO.getNumberOfChildren() : existingTrip.getNumberOfChildren());
        
        // Update budget limits if provided
        if (tripRequestDTO.getTotalBudgetLimit() != null) {
            // Validate that budget limit is not negative
            if (tripRequestDTO.getTotalBudgetLimit().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Budget limit cannot be negative");
            }
            
            // Validate that the new budget limit is not below the current total budget (amount spent)
            if (tripRequestDTO.getTotalBudgetLimit().compareTo(existingTrip.getTotalBudget()) < 0) {
                throw new BadRequestException(
                    "Budget limit cannot be set below the current total budget (Rs. " + 
                    existingTrip.getTotalBudget() + "). You have already spent more than the proposed limit."
                );
            }
            existingTrip.setTotalBudgetLimit(tripRequestDTO.getTotalBudgetLimit());
        }
        
        // Update trip status if provided
        if (tripRequestDTO.getTripStatus() != null) {
            existingTrip.setTripStatus(tripRequestDTO.getTripStatus());
        }

        // Update locations if provided
        if (tripRequestDTO.getLocations() != null && !tripRequestDTO.getLocations().isEmpty()) {
            List<Location> updatedLocations = setLocationsForTrip(tripRequestDTO.getLocations(), tripRequestDTO.getStartLocation());
            existingTrip.setLocations(updatedLocations);
            if (tripRequestDTO.getStartLocation() != null) {
                existingTrip.setStartLocation(updatedLocations.getFirst());
            }
        }

        // Update budget category limits if provided
        if (tripRequestDTO.getAccommodationLimit() != null || 
            tripRequestDTO.getFoodLimit() != null || 
            tripRequestDTO.getTransportLimit() != null || 
            tripRequestDTO.getActivityLimit() != null || 
            tripRequestDTO.getMiscellaneousLimit() != null || 
            tripRequestDTO.getShoppingLimit() != null) {
            
            // Clear existing budget categories
            existingTrip.getTripBudgetCategories().clear();
            
            // Set new budget categories
            Set<TripBudgetCategory> updatedCategories = initializeCategoryLimits(tripRequestDTO, existingTrip);
            existingTrip.setTripBudgetCategories(updatedCategories);
        }

        // Save the updated trip
        Trip updatedTrip = tripRepository.save(existingTrip);

        // Convert to response DTO
        TripResponseDTO responseDTO = modelMapper.map(updatedTrip, TripResponseDTO.class);

        return new APIResponse<>(true, "Trip updated successfully", responseDTO);
    }

    private List<Location> setLocationsForTrip(List<LocationDTO> locationDtos, LocationDTO startLocation) {
        List<Location> locations = new ArrayList<>();
        // Convert start location DTO to entity and add to locations
        if (startLocation != null) {
            locations.add(modelMapper.map(startLocation, Location.class));
        }
        for (LocationDTO locationDto : locationDtos) {
            locations.add(modelMapper.map(locationDto, Location.class));
        }

        // Save locations to the database
        return locationRepository.saveAll(locations);
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

        List<Trip> trips = tripRepository.findByParticipants_Tourist(tourist);
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
    @Transactional(readOnly = true)
    public APIResponse<List<TripParticipantDto>> getTripParticipants(Long tripId) {
        log.info("Fetching participants for trip with ID: {}", tripId);
        Trip trip = tripRepository.findByTripId(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip", tripId));

        List<TripParticipantDto> participantDTOs = new ArrayList<>();
        for (TripParticipant participant : trip.getParticipants()) {
            TripParticipantDto dto = TripParticipantDto.builder()
                    .participantId(participant.getParticipantId())
                    .firstName(participant.getTourist().getFirstName())
                    .lastName(participant.getTourist().getLastName())
                    // .tripId(trip.getTripId())
                    .role(participant.getTripRole().name())
                    .build();
            participantDTOs.add(dto);
        }

        return new APIResponse<>(true, "Trip participants fetched successfully", participantDTOs);
    }

    @Override
    @Transactional
    public APIResponse<TripResponseDTO> removeTouristFromTrip(Long tripId, Long touristId) {
        log.info("Removing tourist with ID: {} from trip with ID: {}", touristId, tripId);

        Trip trip = tripRepository.findByTripId(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip", tripId));

        //Get the logged-in user Trip Role
        TripParticipant loggedInParticipant = trip.getParticipants().stream()
                .filter(participant -> participant.getTourist().getUserId().equals(authUtils.loggedInUserId()))
                .findFirst()
                .orElseThrow(() -> new UserNotFoundException("Logged-in user is not a participant of this trip"));

        // Validate participant has permission to remove tourist
        if (!tripPrivilegeUtils.hasPrivilege(loggedInParticipant.getTripRole(), TripPrivilege.REMOVE_MEMBERS)) {
            throw new BadRequestException("Only admins can remove tourists from the trip");
        }

        // Check if the tourist is part of the trip
        TripParticipant participantToRemove = trip.getParticipants().stream()
                .filter(participant -> participant.getTourist().getUserId().equals(touristId))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Tourist is not part of this trip"));

        // Remove the tourist from the trip
        trip.getParticipants().remove(participantToRemove);
        Trip updatedTrip = tripRepository.save(trip);

        TripResponseDTO tripResponseDTO = modelMapper.map(updatedTrip, TripResponseDTO.class);

        // Update the chat room for the trip
        ChatRoomDto chatRoomDto = chatRoomService.setChatRoomForTrip(trip);
        tripResponseDTO.setChatRoom(chatRoomDto);

        return new APIResponse<>(true, "Tourist removed from trip successfully", tripResponseDTO);
    }

    @Override
    public APIResponse<List<TripPeriodDto>> getMyTripPeriod() {
        log.info("Fetching trip period for the logged-in user");
        User currentUser = touristRepository.findById(authUtils.loggedInUserId())
                .orElseThrow(() -> new UserNotFoundException("Tourist not found with id: " + authUtils.loggedInUserId()));
        if (!(currentUser instanceof Tourist tourist)) {
            throw new IllegalStateException("Only tourists can fetch their trip period");
        }

        List<Trip> trips = tripRepository.findByParticipants_Tourist(tourist);
        if (trips.isEmpty()) {
            return new APIResponse<>(false, "No trips found for the user", null);
        }
        List<TripPeriodDto> tripPeriods = new ArrayList<>();
        for (Trip trip : trips) {
            tripPeriods.add(modelMapper.map(trip, TripPeriodDto.class));
        }

        return new APIResponse<>(true, "Trip periods fetched successfully", tripPeriods);
    }

    private Set<TripBudgetCategory> initializeCategoryLimits(TripRequestDTO tripRequestDTO, Trip trip) {
        Set<TripBudgetCategory> categoryLimits = new HashSet<>();

        if (tripRequestDTO.getAccommodationLimit() != null) {
            categoryLimits.add(new TripBudgetCategory(
                    BudgetCategory.ACCOMMODATION, tripRequestDTO.getAccommodationLimit(), trip));
        }
        if (tripRequestDTO.getFoodLimit() != null) {
            categoryLimits.add(new TripBudgetCategory(
                    BudgetCategory.FOOD, tripRequestDTO.getFoodLimit(), trip));
        }
        if (tripRequestDTO.getTransportLimit() != null) {
            categoryLimits.add(new TripBudgetCategory(
                    BudgetCategory.TRANSPORT, tripRequestDTO.getTransportLimit(), trip));
        }
        if (tripRequestDTO.getActivityLimit() != null) {
            categoryLimits.add(new TripBudgetCategory(
                    BudgetCategory.ACTIVITY, tripRequestDTO.getActivityLimit(), trip));
        }
        if (tripRequestDTO.getMiscellaneousLimit() != null) {
            categoryLimits.add(new TripBudgetCategory(
                    BudgetCategory.MISCELLANEOUS, tripRequestDTO.getMiscellaneousLimit(), trip));
        }
        if (tripRequestDTO.getShoppingLimit() != null) {
            categoryLimits.add(new TripBudgetCategory(
                    BudgetCategory.SHOPPING, tripRequestDTO.getShoppingLimit(), trip));
        }

        return categoryLimits;
    }

    /**
     * Validates that the new trip start and end dates don't conflict with existing trip items
     * - Trip start date cannot be after the first trip item start date
     * - Trip end date cannot be before the last trip item end date
     */
    private void validateTripDatesAgainstTripItems(Long tripId, LocalDate newStartDate, LocalDate newEndDate) {
        // Find the earliest start time among trip items
        Optional<LocalDateTime> earliestStartTime = tripItemRepository.findEarliestStartTimeByTripId(tripId);
        if (earliestStartTime.isPresent()) {
            LocalDate earliestStartDate = earliestStartTime.get().toLocalDate();
            if (newStartDate.isAfter(earliestStartDate)) {
                throw new BadRequestException(
                    "Trip start date cannot be after the first trip item start date (" + earliestStartDate + ")"
                );
            }
        }

        // Find the latest end time among trip items
        Optional<LocalDateTime> latestEndTime = tripItemRepository.findLatestEndTimeByTripId(tripId);
        if (latestEndTime.isPresent()) {
            LocalDate latestEndDate = latestEndTime.get().toLocalDate();
            if (newEndDate.isBefore(latestEndDate)) {
                throw new BadRequestException(
                    "Trip end date cannot be before the last trip item end date (" + latestEndDate + ")"
                );
            }
        }
    }
}
