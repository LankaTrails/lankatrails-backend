package com.lankatrails.lankatrails_backend.service.impl;

import com.lankatrails.lankatrails_backend.dtos.request.TripBudgetLimitDto;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.exception.BadRequestException;
import com.lankatrails.lankatrails_backend.model.Trip;
import com.lankatrails.lankatrails_backend.model.TripBudgetCategory;
import com.lankatrails.lankatrails_backend.model.TripExpense;
import com.lankatrails.lankatrails_backend.model.enums.BudgetCategory;
import com.lankatrails.lankatrails_backend.model.enums.TripPrivilege;
import com.lankatrails.lankatrails_backend.repositories.TripBudgetCategoryRepository;
import com.lankatrails.lankatrails_backend.repositories.TripRepository;
import com.lankatrails.lankatrails_backend.security.utils.AuthUtils;
import com.lankatrails.lankatrails_backend.service.TripBudgetService;
import com.lankatrails.lankatrails_backend.service.utils.BudgetValidator;
import com.lankatrails.lankatrails_backend.service.utils.TripPrivilegeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TripBudgetServiceImpl implements TripBudgetService {
    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private AuthUtils authUtils;

    @Autowired
    private BudgetValidator budgetValidator;

    @Autowired
    private TripBudgetCategoryRepository tripBudgetCategoryRepository;

    @Override
    @Transactional
    public APIResponse<TripBudgetLimitDto> addTripBudgetLimit(TripBudgetLimitDto dto) {
        log.info("Adding trip budget limit: {}", dto);
        Long userId = authUtils.loggedInUserId();

        Trip trip = tripRepository.findById(dto.getTripId())
                .orElseThrow(() -> new IllegalArgumentException("Trip not found with ID: " + dto.getTripId()));

        // Validation order
        budgetValidator.validateLimitAmount(dto.getLimitAmount());
        budgetValidator.validateUserPrivileges(trip, userId, TripPrivilege.SET_BUDGET_LIMITS);

        // Check spent (derive only if not already stored)
        Double spentAmount = trip.getTripBudgetCategories().stream()
                .filter(c -> c.getBudgetCategory() == dto.getBudgetCategory())
                .map(TripBudgetCategory::getSpentAmount)
                .findFirst()
                .orElse(getTotalSpentAmountByCategory(trip, dto.getBudgetCategory()));

        budgetValidator.validateAgainstSpent(spentAmount, dto.getLimitAmount());

        // Validate category sum against trip total
        double newCategoryTotal = getTotalCategoryLimit(trip) + dto.getLimitAmount();
        budgetValidator.validateAgainstTotalBudget(trip, newCategoryTotal);

        // Prevent duplicates
        budgetValidator.ensureCategoryNotExists(trip, dto.getBudgetCategory());

        // Save
        TripBudgetCategory category = modelMapper.map(dto, TripBudgetCategory.class);
        category.setTrip(trip);
        TripBudgetCategory saved = tripBudgetCategoryRepository.save(category);

        return new APIResponse<>(true, "Trip budget limit added successfully",
                modelMapper.map(saved, TripBudgetLimitDto.class));
    }

    @Override
    @Transactional
    public APIResponse<TripBudgetLimitDto> updateTripBudgetLimit(TripBudgetLimitDto dto) {
        log.info("Updating trip budget limit: {}", dto);
        Long userId = authUtils.loggedInUserId();

        Trip trip = tripRepository.findById(dto.getTripId())
                .orElseThrow(() -> new IllegalArgumentException("Trip not found with ID: " + dto.getTripId()));

        // Validation order
        budgetValidator.validateLimitAmount(dto.getLimitAmount());
        budgetValidator.validateUserPrivileges(trip, userId, TripPrivilege.SET_BUDGET_LIMITS);

        TripBudgetCategory existing = tripBudgetCategoryRepository.findById(dto.getLimitId())
                .orElseThrow(() -> new IllegalArgumentException("Budget limit not found with ID: " + dto.getLimitId()));

        // Validate that the category matches
        if (existing.getBudgetCategory() != dto.getBudgetCategory()) {
            throw new BadRequestException("Cannot update budget limit category - category mismatch");
        }

        // Validate spent vs new limit
        budgetValidator.validateAgainstSpent(existing.getSpentAmount(), dto.getLimitAmount());

        // Validate category totals
        double newCategoryTotal = getTotalCategoryLimit(trip) - existing.getLimitAmount() + dto.getLimitAmount();
        budgetValidator.validateAgainstTotalBudget(trip, newCategoryTotal);

        // Update + save
        existing.setLimitAmount(dto.getLimitAmount());
        TripBudgetCategory updated = tripBudgetCategoryRepository.save(existing);

        return new APIResponse<>(true, "Trip budget limit updated successfully",
                modelMapper.map(updated, TripBudgetLimitDto.class));
    }

    @Override
    public APIResponse<List<TripBudgetLimitDto>> getTripBudgetLimitsByTripId(Long tripId) {
        log.info("Fetching trip budget limits for trip ID: {}", tripId);
        Long userId = authUtils.loggedInUserId();

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found with ID: " + tripId));

        List<TripBudgetLimitDto> result = tripBudgetCategoryRepository.findByTripTripId(tripId)
                .stream()
                .map(limit -> modelMapper.map(limit, TripBudgetLimitDto.class))
                .toList();

        return new APIResponse<>(true, "Trip budget limits fetched successfully", result);
    }

    private Double getTotalSpentAmountByCategory(Trip trip, BudgetCategory category) {
        return trip.getTripExpenses().stream()
                .filter(expense -> expense.getBudgetCategory().equals(category))
                .mapToDouble(TripExpense::getAmount)
                .sum();
    }

    private Double getTotalCategoryLimit(Trip trip) {
        return trip.getTripBudgetCategories().stream()
                .mapToDouble(TripBudgetCategory::getLimitAmount)
                .sum();
    }
}

