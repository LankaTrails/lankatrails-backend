package com.lankatrails.lankatrails_backend.service.impl;

import com.lankatrails.lankatrails_backend.dtos.ChatRoomDto;
import com.lankatrails.lankatrails_backend.dtos.ExpenseDTO;
import com.lankatrails.lankatrails_backend.dtos.ExpenseShareDto;
import com.lankatrails.lankatrails_backend.dtos.TripParticipantDto;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.ExpenseResponseDTO;
import com.lankatrails.lankatrails_backend.dtos.response.TripResponseDTO;
import com.lankatrails.lankatrails_backend.exception.BadRequestException;
import com.lankatrails.lankatrails_backend.exception.UserNotFoundException;
import com.lankatrails.lankatrails_backend.model.*;
import com.lankatrails.lankatrails_backend.model.enums.BudgetCategory;
import com.lankatrails.lankatrails_backend.model.enums.TripPrivilege;
import com.lankatrails.lankatrails_backend.repositories.TripBudgetCategoryRepository;
import com.lankatrails.lankatrails_backend.repositories.TripExpenseRepository;
import com.lankatrails.lankatrails_backend.repositories.TripExpenseShareRepository;
import com.lankatrails.lankatrails_backend.repositories.TripRepository;
import com.lankatrails.lankatrails_backend.security.utils.AuthUtils;
import com.lankatrails.lankatrails_backend.service.ChatRoomService;
import com.lankatrails.lankatrails_backend.service.TripExpenseService;
import com.lankatrails.lankatrails_backend.service.utils.TripPrivilegeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TripExpenseServiceImpl implements TripExpenseService {

    private final TripRepository tripRepository;
    private final TripBudgetCategoryRepository tripBudgetCategoryRepository;
    private final TripExpenseRepository tripExpenseRepository;
    private final TripExpenseShareRepository tripExpenseShareRepository;
    private final ModelMapper modelMapper;
    private final AuthUtils authUtils;
    private final ChatRoomService chatRoomService;
    private final TripPrivilegeUtils tripPrivilegeUtils;

    /**
     * Find a participant in a trip by their user ID
     *
     * @param trip    The trip containing the participants
     * @param userId  The user ID to look for
     * @param context Additional context for error messages
     * @return The found TripParticipant
     * @throws BadRequestException if participant not found
     */
    private TripParticipant findParticipantByUserId(Trip trip, Long userId, String context) {
        log.info("{} - Looking for participant with user ID: {}", context, userId);
        return trip.getParticipants().stream()
                .filter(p -> {
                    Long participantUserId = p.getTourist().getUserId();
                    log.info("{} - Checking participant. Tourist ID: {}, Target ID: {}",
                            context, participantUserId, userId);
                    return participantUserId.equals(userId);
                })
                .findFirst()
                .orElseThrow(() -> new BadRequestException(
                        String.format("%s - Participant not found for user ID: %d", context, userId)));
    }

    @Override
    @Transactional
    public APIResponse<String> createExpense(ExpenseDTO expenseDTO) {
        log.info("Creating expense: {}", expenseDTO);

        // Get logged-in user's ID
        Long loggedInUserId = authUtils.loggedInUserId();
        log.info("Logged in user ID: {}", loggedInUserId);

        // Validate trip exists
        Trip trip = tripRepository.findById(expenseDTO.getTripId())
                .orElseThrow(() -> new BadRequestException("Trip not found for the given id"));

        // Validate logged-in user is a participant of the trip
        TripParticipant loggedInParticipant = findParticipantByUserId(trip, loggedInUserId, "Create Expense");

        // Validate participant has permission to create expenses
        if (!tripPrivilegeUtils.hasPrivilege(loggedInParticipant.getTripRole(), TripPrivilege.ADD_EXPENSES)) {
            throw new BadRequestException("Only participants with CREATE_EXPENSES privilege can create expenses");
        }

        // Validate expense data
        if (expenseDTO.getExpenseName() == null || expenseDTO.getExpenseName().isEmpty()) {
            throw new BadRequestException("Expense name cannot be empty");
        }

        // Validate total expense amount first
        Double totalExpenseAmount = expenseDTO.getTotalExpenseAmount();
        if (totalExpenseAmount == null || totalExpenseAmount <= 0) {
            throw new BadRequestException("Total expense amount must be greater than zero");
        }

        // Validate shares if provided
        if (expenseDTO.getShares() != null && !expenseDTO.getShares().isEmpty()) {
            Double calculatedTotal = expenseDTO.getShares().stream()
                    .mapToDouble(ExpenseShareDto::getAmount)
                    .sum();

            if (Math.abs(calculatedTotal - totalExpenseAmount) > 0.01) { // Using small delta for double comparison
                throw new BadRequestException("Total expense amount does not match the sum of shares");
            }

            // Validate each share has a valid amount and participant
            for (ExpenseShareDto shareDto : expenseDTO.getShares()) {
                if (shareDto.getAmount() == null || shareDto.getAmount() <= 0) {
                    throw new BadRequestException("Share amount must be greater than zero");
                }
                if (shareDto.getParticipant() == null || shareDto.getParticipant().getParticipantId() == null) {
                    throw new BadRequestException("Each share must have a valid participant");
                }
            }
        }

        // Validate budget category
        String budgetCategoryStr = expenseDTO.getBudgetCategory();
        if (budgetCategoryStr == null) {
            throw new BadRequestException("Budget category is required");
        }

        // Validate that the budget category string is a valid enum value
        BudgetCategory budgetCategoryEnum;
        try {
            budgetCategoryEnum = BudgetCategory.valueOf(budgetCategoryStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid budget category: " + budgetCategoryStr);
        }

        // Find or create budget category for this trip
        TripBudgetCategory existingCategory = trip.getTripBudgetCategories().stream()
                .filter(category -> category.getBudgetCategory().name().equalsIgnoreCase(budgetCategoryStr))
                .findFirst()
                .orElse(null);

        boolean isNewCategory = false;
        // If category doesn't exist, create one with no limit (0.0)
        if (existingCategory == null) {
            existingCategory = new TripBudgetCategory();
            existingCategory.setBudgetCategory(budgetCategoryEnum);
            existingCategory.setLimitAmount(BigDecimal.ZERO); // No budget limit
            existingCategory.setSpentAmount(BigDecimal.ZERO);
            existingCategory.setTrip(trip);
            // Save the new category
            existingCategory = tripBudgetCategoryRepository.save(existingCategory);
            isNewCategory = true;
            log.info("Created new budget category {} for trip {} with no limit", budgetCategoryEnum, trip.getTripId());
        }

        // Only validate budget limit if there's actually a limit set (> 0)
        if (existingCategory.getLimitAmount().compareTo(BigDecimal.ZERO) > 0 &&
                existingCategory.getLimitAmount().compareTo(existingCategory.getSpentAmount().add(BigDecimal.valueOf(totalExpenseAmount))) < 0) {
            throw new BadRequestException("Expense exceeds the budget limit for category: " + existingCategory.getBudgetCategory());
        }

        // Calculate total spent amount for the trip
        BigDecimal tripTotalSpent = getTotalSpentAmount(trip);
        if (trip.getTotalBudgetLimit().compareTo(tripTotalSpent.add(BigDecimal.valueOf(totalExpenseAmount))) < 0) {
            throw new BadRequestException("Expense exceeds the total budget limit for the trip");
        }

        // Create new expense entity
        TripExpense tripExpense = new TripExpense();
        tripExpense.setExpenseName(expenseDTO.getExpenseName());
        tripExpense.setBudgetCategory(budgetCategoryEnum);
        tripExpense.setExpenseDateTime(LocalDateTime.now());
        tripExpense.setTrip(trip);
        tripExpense.setCreatedByParticipant(loggedInParticipant);
        tripExpense.setAmount(BigDecimal.valueOf(totalExpenseAmount)); // Set the amount field
        tripExpense.setTotalExpenseAmount(totalExpenseAmount);

        // Update category spent amount
        existingCategory.setSpentAmount(existingCategory.getSpentAmount().add(BigDecimal.valueOf(totalExpenseAmount)));

        // Only add to trip's categories if it's newly created
        if (isNewCategory) {
            trip.getTripBudgetCategories().add(existingCategory);
        }

        // Update trip total spent amount - we'll use a helper method to calculate this
        updateTripTotalSpentAmount(trip, BigDecimal.valueOf(totalExpenseAmount));
        TripExpense savedExpense = tripExpenseRepository.save(tripExpense);

        // Create ExpenseShare entities
        if (expenseDTO.getShares() == null || expenseDTO.getShares().isEmpty()) {
            log.info("No shares provided. Creating default share for logged-in user with ID: {}",
                    loggedInParticipant.getTourist().getUserId());

            // Create default share for logged-in user
            tripExpenseShareRepository.save(TripExpenseShare.builder()
                    .amount(totalExpenseAmount)
                    .tripExpense(savedExpense)
                    .tripParticipant(loggedInParticipant)
                    .build());
        } else {
            // Create shares for each participant
            for (ExpenseShareDto shareDto : expenseDTO.getShares()) {
                Long participantId = shareDto.getParticipant().getParticipantId();
                log.info("Processing share for participant ID: {}", participantId);

                // Find participant in trip by their participant ID
                TripParticipant participant = trip.getParticipants().stream()
                        .filter(p -> p.getParticipantId().equals(participantId))
                        .findFirst()
                        .orElseThrow(() -> new BadRequestException(
                                String.format("Create Expense Share - Participant not found for ID: %d", participantId)));

                // Create and save share
                tripExpenseShareRepository.save(TripExpenseShare.builder()
                        .amount(shareDto.getAmount())
                        .tripExpense(savedExpense)
                        .tripParticipant(participant)
                        .build());
            }
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
    @Transactional
    public APIResponse<String> updateExpense(Long expenseId, ExpenseDTO expenseDTO) {
        log.info("Updating expense with ID: {}", expenseId);

        // Validate expense exists
        TripExpense existingExpense = tripExpenseRepository.findById(expenseId)
                .orElseThrow(() -> new BadRequestException("Expense not found for the given id"));

        // Validate expense is through the app
        if (existingExpense.getIsThroughApp()) {
            throw new BadRequestException(" Expenses created through the app can't be updated");
        }

        Trip trip = existingExpense.getTrip();

        // Validate logged-in user is a participant of the trip
        TripParticipant loggedInParticipant = trip.getParticipants().stream()
                .filter(participant -> participant.getTourist().getUserId().equals(authUtils.loggedInUserId()))
                .findFirst()
                .orElseThrow(() -> new UserNotFoundException("Logged-in user is not a participant of this trip"));

        // Validate participant has permission to update expenses (either creator or has privilege)
        boolean canUpdate = existingExpense.getCreatedByParticipant().getTourist().getUserId().equals(authUtils.loggedInUserId()) ||
                tripPrivilegeUtils.hasPrivilege(loggedInParticipant.getTripRole(), TripPrivilege.ADD_EXPENSES);

        if (!canUpdate) {
            throw new BadRequestException("Only the expense creator or participants with ADD_EXPENSES privilege can update expenses");
        }

        // Validate expense data
        if (expenseDTO.getExpenseName() == null || expenseDTO.getExpenseName().isEmpty()) {
            throw new BadRequestException("Expense name cannot be empty");
        }

        // Validate total expense amount first
        Double newTotalAmount = expenseDTO.getTotalExpenseAmount();
        if (newTotalAmount == null || newTotalAmount <= 0) {
            throw new BadRequestException("Total expense amount must be greater than zero");
        }

        // Validate shares if provided
        if (expenseDTO.getShares() != null && !expenseDTO.getShares().isEmpty()) {
            Double calculatedTotal = expenseDTO.getShares().stream()
                    .mapToDouble(ExpenseShareDto::getAmount)
                    .sum();

            if (Math.abs(calculatedTotal - newTotalAmount) > 0.01) { // Using small delta for double comparison
                throw new BadRequestException("Total expense amount does not match the sum of shares");
            }

            // Validate each share has a valid amount and participant
            for (ExpenseShareDto shareDto : expenseDTO.getShares()) {
                if (shareDto.getAmount() == null || shareDto.getAmount() <= 0) {
                    throw new BadRequestException("Share amount must be greater than zero");
                }
                if (shareDto.getParticipant() == null || shareDto.getParticipant().getParticipantId() == null) {
                    throw new BadRequestException("Each share must have a valid participant");
                }
            }
        }

        // Validate budget category
        String budgetCategoryStr = expenseDTO.getBudgetCategory();
        if (budgetCategoryStr == null) {
            throw new BadRequestException("Budget category is required");
        }

        // Get old values
        Double oldTotalAmount = existingExpense.getTotalExpenseAmount();
        BudgetCategory oldCategory = existingExpense.getBudgetCategory();

        // Validate and convert new budget category
        BudgetCategory newCategory;
        try {
            newCategory = BudgetCategory.valueOf(budgetCategoryStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid budget category: " + budgetCategoryStr);
        }

        // Find budget category entities
        TripBudgetCategory oldBudgetCategory = trip.getTripBudgetCategories().stream()
                .filter(category -> category.getBudgetCategory() == oldCategory)
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Old budget category not found"));

        TripBudgetCategory newBudgetCategory = trip.getTripBudgetCategories().stream()
                .filter(category -> category.getBudgetCategory() == newCategory)
                .findFirst()
                .orElseThrow(() -> new BadRequestException("New budget category not found"));

        // Calculate spending differences
        Double amountDifference = newTotalAmount - oldTotalAmount;

        // Validate expense does not exceed category limit for new category
        if (oldCategory.equals(newCategory)) {
            // Same category - check if the difference doesn't exceed limit
            if (newBudgetCategory.getLimitAmount().compareTo(newBudgetCategory.getSpentAmount().add(BigDecimal.valueOf(amountDifference))) < 0) {
                throw new BadRequestException("Updated expense exceeds the budget limit for category: " + newBudgetCategory.getBudgetCategory());
            }
        } else {
            // Different category - check if new category can accommodate the full new amount
            if (newBudgetCategory.getLimitAmount().compareTo(newBudgetCategory.getSpentAmount().add(BigDecimal.valueOf(newTotalAmount))) < 0) {
                throw new BadRequestException("Updated expense exceeds the budget limit for category: " + newBudgetCategory.getBudgetCategory());
            }
        }

        // Validate total trip budget
        BigDecimal tripTotalSpent = getTotalSpentAmount(trip);
        if (trip.getTotalBudgetLimit().compareTo(tripTotalSpent.add(BigDecimal.valueOf(amountDifference))) < 0) {
            throw new BadRequestException("Updated expense exceeds the total budget limit for the trip");
        }

        // Update expense entity
        existingExpense.setExpenseName(expenseDTO.getExpenseName());
        existingExpense.setBudgetCategory(newCategory);
        existingExpense.setAmount(BigDecimal.valueOf(newTotalAmount)); // Set the amount field
        existingExpense.setTotalExpenseAmount(newTotalAmount);

        // Update budget categories spent amounts
        if (oldCategory.equals(newCategory)) {
            // Same category - just update the difference
            oldBudgetCategory.setSpentAmount(oldBudgetCategory.getSpentAmount().add(BigDecimal.valueOf(amountDifference)));
        } else {
            // Different categories - remove from old, add to new
            oldBudgetCategory.setSpentAmount(oldBudgetCategory.getSpentAmount().subtract(BigDecimal.valueOf(oldTotalAmount)));
            newBudgetCategory.setSpentAmount(newBudgetCategory.getSpentAmount().add(BigDecimal.valueOf(newTotalAmount)));
        }

        // Update trip total spent amount - use helper method for consistency
        updateTripTotalSpentAmount(trip, BigDecimal.valueOf(amountDifference));

        // Delete existing expense shares
        tripExpenseShareRepository.deleteByTripExpense(existingExpense);

        // Create new expense shares
        if (expenseDTO.getShares() == null || expenseDTO.getShares().isEmpty()) {
            // Create default share for creator
            tripExpenseShareRepository.save(TripExpenseShare.builder()
                    .amount(newTotalAmount)
                    .tripExpense(existingExpense)
                    .tripParticipant(loggedInParticipant)
                    .build());
        } else {
            // Create shares for each participant
            for (ExpenseShareDto shareDto : expenseDTO.getShares()) {
                // Find participant in trip
                TripParticipant participant = trip.getParticipants().stream()
                        .filter(p -> p.getParticipantId().equals(shareDto.getParticipant().getParticipantId()))
                        .findFirst()
                        .orElseThrow(() -> new BadRequestException("Participant not found for ID: " + shareDto.getParticipant().getParticipantId()));

                // Create and save share
                tripExpenseShareRepository.save(TripExpenseShare.builder()
                        .amount(shareDto.getAmount())
                        .tripExpense(existingExpense)
                        .tripParticipant(participant)
                        .build());
            }
        }

        // Save updated entities
        tripExpenseRepository.save(existingExpense);
        tripRepository.save(trip);

        log.info("Expense updated successfully for expense ID: {}", expenseId);
        return new APIResponse<>(true, "Expense updated successfully", "Expense updated successfully for expense ID: " + expenseId);
    }

    // @Override
    // @Transactional
    // public APIResponse<String> deleteExpense(Long expenseId) {
    //     log.info("Deleting expense with ID: {}", expenseId);

    //     // Validate expense exists
    //     TripExpense existingExpense = tripExpenseRepository.findById(expenseId)
    //             .orElseThrow(() -> new BadRequestException("Expense not found for the given id"));

    //     Trip trip = existingExpense.getTrip();

    //     // Validate logged-in user is a participant of the trip
    //     TripParticipant loggedInParticipant = trip.getParticipants().stream()
    //             .filter(participant -> participant.getTourist().getUserId().equals(authUtils.loggedInUserId()))
    //             .findFirst()
    //             .orElseThrow(() -> new UserNotFoundException("Logged-in user is not a participant of this trip"));

    //     // Validate participant has permission to delete expenses (either creator or has privilege)
    //     boolean canDelete = existingExpense.getCreatedByParticipant().getTourist().getUserId().equals(authUtils.loggedInUserId()) ||
    //             tripPrivilegeUtils.hasPrivilege(loggedInParticipant.getTripRole(), TripPrivilege.EDIT_EXPENSES);

    //     if (!canDelete) {
    //         throw new BadRequestException("Only the expense creator or participants with EDIT_EXPENSES privilege can delete expenses");
    //     }

    //     // Get expense details for rollback
    //     Double expenseAmount = existingExpense.getTotalExpenseAmount();
    //     BudgetCategory budgetCategory = existingExpense.getBudgetCategory();

    //     // Find budget category
    //     TripBudgetCategory existingBudgetCategory = trip.getTripBudgetCategories().stream()
    //             .filter(category -> category.getBudgetCategory() == budgetCategory)
    //             .findFirst()
    //             .orElseThrow(() -> new BadRequestException("Budget category not found"));

    //     // Delete expense shares first (due to foreign key constraints)
    //     tripExpenseShareRepository.

    //     // Delete the expense
    //     tripExpenseRepository.delete(existingExpense);

    //     // Update budget category spent amount (subtract the deleted expense amount)
    //     existingBudgetCategory.setSpentAmount(existingBudgetCategory.getSpentAmount() - expenseAmount);

    //     // Update trip total spent amount (subtract the deleted expense amount)
    //     trip.setTotalSpentAmount(trip.getTotalSpentAmount() - expenseAmount);

    //     // Save updated trip
    //     tripRepository.save(trip);

    //     log.info("Expense deleted successfully for expense ID: {}", expenseId);
    //     return new APIResponse<>(true, "Expense deleted successfully", "Expense deleted successfully for expense ID: " + expenseId);
    // }


    @Override
    @Transactional
    public APIResponse<String> deleteExpense(Long expenseId) {
        log.info("Deleting expense with ID: {}", expenseId);

        // Validate expense exists
        TripExpense existingExpense = tripExpenseRepository.findById(expenseId)
                .orElseThrow(() -> new BadRequestException("Expense not found for the given id"));

        // Validate expense is through the app
        if (existingExpense.getIsThroughApp()) {
            throw new BadRequestException(" Expenses created through the app can't be deleted");
        }

        tripExpenseRepository.delete(existingExpense);

        return new APIResponse<>(true, "Expense deleted successfully", "Expense deleted successfully for expense ID: " + expenseId);
    }

    @Override
    @Transactional(readOnly = true)
    public APIResponse<List<ExpenseResponseDTO>> getExpensesByTripId(Long tripId) {
        log.info("Fetching expenses for trip with ID: {}", tripId);

        // Validate trip exists
        if (!tripRepository.existsById(tripId)) {
            throw new BadRequestException("Trip not found for the given id");
        }

        // Get expenses for the trip using correct repository method
        List<TripExpense> expenses = tripExpenseRepository.findByTrip_TripId(tripId);
        log.info("Found {} expenses for trip ID: {}", expenses.size(), tripId);

        // Convert to response DTOs
        List<ExpenseResponseDTO> responseDTOs = new ArrayList<>();
        for (TripExpense expense : expenses) {
            ExpenseResponseDTO responseDTO = new ExpenseResponseDTO();
            responseDTO.setExpenseId(expense.getExpenseId());
            responseDTO.setExpenseName(expense.getExpenseName());
            responseDTO.setTotalExpenseAmount(expense.getTotalExpenseAmount()); // Set total expense amount
            responseDTO.setBudgetCategory(expense.getBudgetCategory().name());
            responseDTO.setTripId(expense.getTrip().getTripId());
            responseDTO.setIsThroughApp(expense.getIsThroughApp());
            responseDTO.setExpenseDateTime(expense.getExpenseDateTime()); // Set the expense date/time

            // Set created by participant
            TripParticipant createdBy = expense.getCreatedByParticipant();
            if (createdBy != null) {
                TripParticipantDto createdByDto = new TripParticipantDto();
                createdByDto.setParticipantId(createdBy.getParticipantId());
                createdByDto.setFirstName(createdBy.getTourist().getFirstName());
                createdByDto.setLastName(createdBy.getTourist().getLastName());
                createdByDto.setProfileImageUrl(createdBy.getTourist().getProfilePictureUrl());
                createdByDto.setRole(createdBy.getTripRole().name());
                responseDTO.setCreatedByParticipant(createdByDto);
            }

            // Set expense shares
            List<ExpenseShareDto> sharesDtos = new ArrayList<>();
            if (expense.getShares() != null) {
                for (TripExpenseShare share : expense.getShares()) {
                    ExpenseShareDto shareDto = new ExpenseShareDto();
                    shareDto.setAmount(share.getAmount());

                    // Set participant info for the share
                    TripParticipantDto participantDto = new TripParticipantDto();
                    participantDto.setParticipantId(share.getTripParticipant().getParticipantId());
                    participantDto.setFirstName(share.getTripParticipant().getTourist().getFirstName());
                    participantDto.setLastName(share.getTripParticipant().getTourist().getLastName());
                    participantDto.setProfileImageUrl(share.getTripParticipant().getTourist().getProfilePictureUrl());
                    participantDto.setRole(share.getTripParticipant().getTripRole().name());
                    shareDto.setParticipant(participantDto);

                    sharesDtos.add(shareDto);
                }
            }
            responseDTO.setShares(sharesDtos);

            responseDTOs.add(responseDTO);
            log.info("Converted expense: ID={}, Name={}, TotalAmount={}, DateTime={}, CreatedBy={}, SharesCount={}",
                    expense.getExpenseId(), expense.getExpenseName(), expense.getTotalExpenseAmount(),
                    expense.getExpenseDateTime(), createdBy != null ? createdBy.getTourist().getFirstName() : "null",
                    sharesDtos.size());
        }

        log.info("Successfully converted {} expenses to response DTOs", responseDTOs.size());
        return new APIResponse<>(true, "Expenses fetched successfully", responseDTOs);
    }

    /**
     * Helper method to calculate total spent amount for a trip
     */
    private BigDecimal getTotalSpentAmount(Trip trip) {
        return trip.getTripExpenses().stream()
                .map(expense -> BigDecimal.valueOf(expense.getTotalExpenseAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Helper method to update trip's total spent amount
     * Note: This method currently calculates the total dynamically from expenses
     * If the Trip entity has a totalSpentAmount field, this method should be updated accordingly
     */
    private void updateTripTotalSpentAmount(Trip trip, BigDecimal additionalAmount) {
        // Currently, we calculate the total spent amount dynamically from expenses
        // If needed, update Trip entity to maintain a totalSpentAmount field
    }
}
