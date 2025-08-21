package com.lankatrails.lankatrails_backend.service.impl;

import com.lankatrails.lankatrails_backend.dtos.ChatRoomDto;
import com.lankatrails.lankatrails_backend.dtos.ExpenseShareDto;
import com.lankatrails.lankatrails_backend.dtos.TripPeriodDto;
import com.lankatrails.lankatrails_backend.dtos.request.LocationDTO;
import com.lankatrails.lankatrails_backend.dtos.request.TripItemDTO;
import com.lankatrails.lankatrails_backend.dtos.request.TripRequestDTO;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.TripResponseDTO;
import com.lankatrails.lankatrails_backend.exception.BadRequestException;
import com.lankatrails.lankatrails_backend.dtos.ExpenseDTO;
import com.lankatrails.lankatrails_backend.dtos.response.ExpenseResponseDTO;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private TripExpenseRepository tripExpenseRepository;

    @Autowired
    private TripExpenseShareRepository tripExpenseShareRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private AuthUtils authUtils;

    @Autowired
    private ChatRoomService chatRoomService;

    @Autowired
    private LocationRepository locationRepository;

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
        if (trip.getTotalSpentAmount() == null) {
            trip.setTotalSpentAmount(0.0);
        }

        // Save the trip
        Trip savedTrip = tripRepository.save(trip);

        // Convert to response DTO
        TripResponseDTO responseDTO = modelMapper.map(savedTrip, TripResponseDTO.class);

        return new APIResponse<>(true, "Trip created successfully", responseDTO);
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

        Tourist tourist = touristRepository.findById(touristId)
                .orElseThrow(() -> new UserNotFoundException("Tourist not found with id: " + touristId));

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

    @Override
    @Transactional
    public APIResponse<String> createExpense(ExpenseDTO expenseDTO) {
        log.info("Creating expense: {}", expenseDTO);

        // Validate trip exists
        Trip trip = tripRepository.findByTripId(expenseDTO.getTripId())
                .orElseThrow(() -> new BadRequestException("Trip not found for the given id"));

        // Validate logged-in user is a participant of the trip
        TripParticipant loggedInParticipant = trip.getParticipants().stream()
                .filter(participant -> participant.getTourist().getUserId().equals(authUtils.loggedInUserId()))
                .findFirst()
                .orElseThrow(() -> new UserNotFoundException("Logged-in user is not a participant of this trip"));

        // Validate participant has permission to create expenses
        if (!tripPrivilegeUtils.hasPrivilege(loggedInParticipant.getTripRole(), TripPrivilege.ADD_EXPENSES)) {
            throw new BadRequestException("Only participants with CREATE_EXPENSES privilege can create expenses");
        }

        // Validate expense data
        if (expenseDTO.getExpenseName() == null || expenseDTO.getExpenseName().isEmpty()) {
            throw new BadRequestException("Expense name cannot be empty");
        }

        // Validate budget category
        TripBudgetCategory existingCategory = trip.getTripBudgetCategories().stream()
                .filter(category -> category.getBudgetCategory().name().equalsIgnoreCase(expenseDTO.getBudgetCategory()))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Invalid budget category"));

        Double totalExpenseAmount = expenseDTO.getShares().stream()
                .mapToDouble(ExpenseShareDto::getAmount)
                .sum();

        // Validate total expense amount
        if (totalExpenseAmount <= 0) {
            throw new BadRequestException("Total expense amount must be greater than zero");
        }

        if (!totalExpenseAmount.equals(expenseDTO.getTotalExpenseAmount())) {
            throw new BadRequestException("Total expense amount does not match the sum of shares");
        }

        // Validate expense does not exceed category limit
        if (existingCategory.getLimitAmount() < existingCategory.getSpentAmount() + totalExpenseAmount) {
            throw new BadRequestException("Expense exceeds the budget limit for category: " + existingCategory.getBudgetCategory());
        }

        if (trip.getTotalBudgetLimit() < trip.getTotalSpentAmount() + totalExpenseAmount) {
            throw new BadRequestException("Expense exceeds the total budget limit for the trip");
        }

        // Create new expense entity
        TripExpense tripExpense = new TripExpense();
        tripExpense.setExpenseName(expenseDTO.getExpenseName());
        tripExpense.setBudgetCategory(BudgetCategory.valueOf(expenseDTO.getBudgetCategory()));
        tripExpense.setExpenseDateTime(LocalDateTime.now());
        tripExpense.setTrip(trip);
        tripExpense.setCreatedByParticipant(loggedInParticipant);
//        tripExpense.setShares(expenseShares);
        tripExpense.setTotalExpenseAmount(totalExpenseAmount);

        // Update category spent amount
        existingCategory.setSpentAmount(existingCategory.getSpentAmount() + totalExpenseAmount);
        trip.getTripBudgetCategories().add(existingCategory);

        // Update trip total spent amount
        trip.setTotalSpentAmount(trip.getTotalSpentAmount() + totalExpenseAmount);
        TripExpense savedExpense =  tripExpenseRepository.save(tripExpense);

        // Create ExpenseShare entities
        for (ExpenseShareDto shareDto : expenseDTO.getShares()) {
            TripParticipant participant = trip.getParticipants().stream()
                    .filter(p -> p.getTourist().getUserId().equals(shareDto.getParticipant().getParticipantId()))
                    .findFirst()
                    .orElseThrow(() -> new BadRequestException("Participant not found for ID: " + shareDto.getParticipant().getParticipantId()));

            TripExpenseShare share = new TripExpenseShare();
            share.setAmount(shareDto.getAmount());
            share.setTripExpense(savedExpense);
            share.setTripParticipant(participant);
            tripExpenseShareRepository.save(share);
        }

        // Save the trip with updated budget categories
        Trip updatedTrip = tripRepository.save(trip);

        // Update the chat room for the trip
        ChatRoomDto chatRoomDto = chatRoomService.setChatRoomForTrip(updatedTrip);
        TripResponseDTO tripResponseDTO = modelMapper.map(updatedTrip, TripResponseDTO.class);
        tripResponseDTO.setChatRoom(chatRoomDto);
        log.info("Expense created successfully for trip ID: {}", trip.getTripId());
        return new APIResponse<>(true, "Expense created successfully", "Expense created successfully for trip ID: " + trip.getTripId());
    }

    @Override
    @Transactional(readOnly = true)
    public APIResponse<List<ExpenseResponseDTO>> getExpensesByTripId(Long tripId) {
        log.info("Fetching expenses for trip with ID: {}", tripId);

        // Validate trip exists
        Trip trip = tripRepository.findByTripId(tripId)
                .orElseThrow(() -> new BadRequestException("Trip not found for the given id"));


        // Get expenses for the trip
//        List<TripExpense> expenses = tripExpenseRepository.findByTripId(tripId);

        // Convert to response DTOs
        List<ExpenseResponseDTO> responseDTOs = new ArrayList<>();
//        for (TripExpense expense : expenses) {
//            ExpenseResponseDTO responseDTO = new ExpenseResponseDTO();
//            responseDTO.setExpenseId(expense.getExpenseId());
//            responseDTO.setExpenseName(expense.getExpenseName());
//            responseDTO.setAmount(expense.getAmount());
//            responseDTO.setBudgetCategory(expense.getBudgetCategory().name());
//            responseDTO.setTripId(expense.getTrip().getTripId());
//            responseDTOs.add(responseDTO);
//        }

        return new APIResponse<>(true, "Expenses fetched successfully", responseDTOs);
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
}
