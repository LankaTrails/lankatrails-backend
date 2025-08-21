package com.lankatrails.lankatrails_backend.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lankatrails.lankatrails_backend.dtos.ChatRoomDto;
import com.lankatrails.lankatrails_backend.dtos.ExpenseDTO;
import com.lankatrails.lankatrails_backend.dtos.ExpenseShareDto;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.ExpenseResponseDTO;
import com.lankatrails.lankatrails_backend.dtos.response.TripResponseDTO;
import com.lankatrails.lankatrails_backend.exception.BadRequestException;
import com.lankatrails.lankatrails_backend.exception.UserNotFoundException;
import com.lankatrails.lankatrails_backend.model.Trip;
import com.lankatrails.lankatrails_backend.model.TripBudgetCategory;
import com.lankatrails.lankatrails_backend.model.TripExpense;
import com.lankatrails.lankatrails_backend.model.TripExpenseShare;
import com.lankatrails.lankatrails_backend.model.TripParticipant;
import com.lankatrails.lankatrails_backend.model.enums.BudgetCategory;
import com.lankatrails.lankatrails_backend.model.enums.TripPrivilege;
import com.lankatrails.lankatrails_backend.repositories.TripExpenseRepository;
import com.lankatrails.lankatrails_backend.repositories.TripExpenseShareRepository;
import com.lankatrails.lankatrails_backend.repositories.TripRepository;
import com.lankatrails.lankatrails_backend.security.utils.AuthUtils;
import com.lankatrails.lankatrails_backend.service.ChatRoomService;
import com.lankatrails.lankatrails_backend.service.TripExpenseService;
import com.lankatrails.lankatrails_backend.service.utils.TripPrivilegeUtils;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TripExpenseServiceImpl implements TripExpenseService {

    @Autowired
    private TripRepository tripRepository;

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
    private TripPrivilegeUtils tripPrivilegeUtils;
    
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
}
