package com.lankatrails.lankatrails_backend.service.impl;

import com.lankatrails.lankatrails_backend.dtos.TripBudgetCategoryDto;
import com.lankatrails.lankatrails_backend.dtos.TripBudgetDto;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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
    public APIResponse<TripBudgetCategoryDto> addTripBudgetLimit(TripBudgetCategoryDto dto) {
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
                modelMapper.map(saved, TripBudgetCategoryDto.class));
    }

    @Override
    @Transactional
    public APIResponse<TripBudgetCategoryDto> updateTripBudgetLimit(TripBudgetCategoryDto dto) {
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
                modelMapper.map(updated, TripBudgetCategoryDto.class));
    }

    @Override
    public APIResponse<List<TripBudgetCategoryDto>> getTripBudgetLimitsByTripId(Long tripId) {
        log.info("Fetching trip budget limits for trip ID: {}", tripId);
        Long userId = authUtils.loggedInUserId();

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found with ID: " + tripId));

        List<TripBudgetCategoryDto> result = tripBudgetCategoryRepository.findByTripTripId(tripId)
                .stream()
                .map(limit -> modelMapper.map(limit, TripBudgetCategoryDto.class))
                .toList();

        return new APIResponse<>(true, "Trip budget limits fetched successfully", result);
    }

    @Override
    @Transactional
    public APIResponse<TripBudgetDto> getTripBudgetDetails(Long tripId) {
        log.info("Fetching trip budget details for trip ID: {}", tripId);
        Long userId = authUtils.loggedInUserId();

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found with ID: " + tripId));

        // Prepare DTO
        TripBudgetDto budgetDto = new TripBudgetDto();
        budgetDto.setTripId(tripId);
        budgetDto.setTotalSpentAmount(trip.getTotalSpentAmount());
        budgetDto.setTotalBudgetLimit(trip.getTotalBudgetLimit());
        budgetDto.setTripBudgetCategories(trip.getTripBudgetCategories().stream()
                .map(category -> modelMapper.map(category, TripBudgetCategoryDto.class))
                .collect(Collectors.toSet()));

        return new APIResponse<>(true, "Trip budget details fetched successfully", budgetDto);
    }

    @Override
    @Transactional
    public APIResponse<TripBudgetDto> updateTripTotalBudget(TripBudgetDto tripBudgetDto) {
        log.info("Updating trip total budget for trip ID: {}", tripBudgetDto.getTripId());
        Long userId = authUtils.loggedInUserId();

        Trip trip = tripRepository.findById(tripBudgetDto.getTripId())
                .orElseThrow(() -> new IllegalArgumentException("Trip not found with ID: " + tripBudgetDto.getTripId()));

        // Validation order
        budgetValidator.validateLimitAmount(tripBudgetDto.getTotalBudgetLimit());
        budgetValidator.validateUserPrivileges(trip, userId, TripPrivilege.SET_BUDGET_LIMITS);

        // Validate against spent amount
        budgetValidator.validateAgainstSpent(trip.getTotalSpentAmount(), tripBudgetDto.getTotalBudgetLimit());
        budgetValidator.validateAgainstTotalBudgetCategory(getTotalCategoryLimit(trip), tripBudgetDto.getTotalBudgetLimit());

        // Update and save
        trip.setTotalBudgetLimit(tripBudgetDto.getTotalBudgetLimit());
        Trip updatedTrip = tripRepository.save(trip);

        TripBudgetDto updatedDto = modelMapper.map(updatedTrip, TripBudgetDto.class);
        updatedDto.setTripBudgetCategories(updatedTrip.getTripBudgetCategories().stream()
                .map(category -> modelMapper.map(category, TripBudgetCategoryDto.class))
                .collect(Collectors.toSet()));

        return new APIResponse<>(true, "Trip total budget updated successfully", updatedDto);
    }

    @Override
    public void updateTripBudgetSpentAmount(Trip trip, Double spentAmount, BudgetCategory budgetCategory) {
        log.info("Updating trip budget spent amount for trip ID: {}, category: {}, amount: {}",
                 trip.getTripId(), budgetCategory, spentAmount);

        // Find the budget category for the given trip and category
        TripBudgetCategory category = trip.getTripBudgetCategories().stream()
                .filter(c -> c.getBudgetCategory() == budgetCategory)
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Budget category not found for trip ID: " + trip.getTripId()));

        // Update the spent amount
        category.setSpentAmount(category.getSpentAmount() + spentAmount);
        tripBudgetCategoryRepository.save(category);

        log.info("Updated spent amount for trip ID: {}, category: {}, new spent amount: {}",
                 trip.getTripId(), budgetCategory, category.getSpentAmount());
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

