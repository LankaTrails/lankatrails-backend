package com.lankatrails.lankatrails_backend.service.impl;

import com.lankatrails.lankatrails_backend.dtos.request.TripBudgetLimitDto;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.exception.BadRequestException;
import com.lankatrails.lankatrails_backend.model.Trip;
import com.lankatrails.lankatrails_backend.model.TripBudgetCategory;
import com.lankatrails.lankatrails_backend.repositories.TripBudgetCategoryRepository;
import com.lankatrails.lankatrails_backend.repositories.TripRepository;
import com.lankatrails.lankatrails_backend.security.utils.AuthUtils;
import com.lankatrails.lankatrails_backend.service.TripBudgetService;
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
    private TripBudgetCategoryRepository tripBudgetCategoryRepository;

    @Override
    @Transactional
    public APIResponse<TripBudgetLimitDto> addTripBudgetLimit(TripBudgetLimitDto tripBudgetLimitDto) {
        log.info("Adding trip budget limit: {}", tripBudgetLimitDto);
        Long touristId = authUtils.loggedInUserId();

        // Validate trip existence
        Trip trip = tripRepository.findById(tripBudgetLimitDto.getTripId())
                .orElseThrow(() -> new IllegalArgumentException("Trip not found with ID: " + tripBudgetLimitDto.getTripId()));

        // Check if the tourist exists in trip's tourist list
        if (trip.getTourists().stream().noneMatch(t -> t.getUserId().equals(touristId))) {
            log.error("Tourist with ID {} is not part of trip with ID {}", touristId, tripBudgetLimitDto.getTripId());
            return new APIResponse<>(false, "Tourist not part of the trip", null);
        }

        // Validate budget limit
        if (tripBudgetLimitDto.getLimitAmount() <= 0) {
            throw new BadRequestException("Limit amount must be greater than zero");
        }

        // Validate with spent amount
//        if (existingLimit.getSpentAmount() > tripBudgetLimitDto.getLimitAmount()) {
//            log.error("Cannot update limit to a value less than spent amount for limit ID {}", tripBudgetLimitDto.getLimitId());
//            throw new BadRequestException("Cannot update limit to a value less than spent amount");
//        }

        // Check if a limit already exists for the given category
        if (trip.getTripBudgetCategories().stream()
                .anyMatch(limit -> limit.getBudgetCategory() == tripBudgetLimitDto.getBudgetCategory())) {
            log.error("Budget limit for category {} already exists for trip ID {}",
                      tripBudgetLimitDto.getBudgetCategory(), tripBudgetLimitDto.getTripId());
            throw new BadRequestException("Budget limit for this category already exists");
        }

        // Map DTO to entity and save
        TripBudgetCategory tripBudgetCategory = modelMapper.map(tripBudgetLimitDto, TripBudgetCategory.class);
        tripBudgetCategory.setTrip(trip);
        TripBudgetCategory savedLimit = tripBudgetCategoryRepository.save(tripBudgetCategory);

        log.info("Trip budget limit added successfully: {}", savedLimit);
        return new APIResponse<>(true, "Trip budget limit added successfully", modelMapper.map(savedLimit, TripBudgetLimitDto.class));
    }

    @Override
    @Transactional
    public APIResponse<TripBudgetLimitDto> updateTripBudgetLimit(TripBudgetLimitDto tripBudgetLimitDto) {
        log.info("Updating trip budget limit: {}", tripBudgetLimitDto);
        Long touristId = authUtils.loggedInUserId();

        // Validate trip existence
        Trip trip = tripRepository.findById(tripBudgetLimitDto.getTripId())
                .orElseThrow(() -> new IllegalArgumentException("Trip not found with ID: " + tripBudgetLimitDto.getTripId()));

        // Check if the tourist exists in trip's tourist list
        if (trip.getTourists().stream().noneMatch(t -> t.getUserId().equals(touristId))) {
            log.error("Tourist with ID {} is not part of trip with ID {}", touristId, tripBudgetLimitDto.getTripId());
            return new APIResponse<>(false, "Tourist not part of the trip", null);
        }

        // Validate budget limit
        if (tripBudgetLimitDto.getLimitAmount() <= 0) {
            throw new BadRequestException("Limit amount must be greater than zero");
        }

        // Find existing limit
        TripBudgetCategory existingLimit = tripBudgetCategoryRepository.findById(tripBudgetLimitDto.getLimitId())
                .orElseThrow(() -> new IllegalArgumentException("Budget limit not found with ID: " + tripBudgetLimitDto.getLimitId()));

        // Validate with spent amount
        if (existingLimit.getSpentAmount() > tripBudgetLimitDto.getLimitAmount()) {
            log.error("Cannot update limit to a value less than spent amount for limit ID {}", tripBudgetLimitDto.getLimitId());
            throw new BadRequestException("Cannot update limit to a value less than spent amount");
        }

        // Validate with total budget limit
        if (trip.getTotalBudgetLimit() > 0 && tripBudgetLimitDto.getLimitAmount() > trip.getTotalBudgetLimit()) {
            log.error("Cannot set limit amount greater than total budget limit for trip ID {}", tripBudgetLimitDto.getTripId());
            throw new BadRequestException("Limit amount cannot exceed total budget limit for the trip");
        }

        // Validate total budget category limits with total budget limit
        double totalBudgetCategoryLimit = trip.getTripBudgetCategories().stream()
                .filter(limit -> !limit.getLimitId().equals(tripBudgetLimitDto.getLimitId()))
                .mapToDouble(TripBudgetCategory::getLimitAmount)
                .sum() + tripBudgetLimitDto.getLimitAmount();

        if (totalBudgetCategoryLimit > trip.getTotalBudgetLimit() && trip.getTotalBudgetLimit() > 0) {
            log.error("Total budget category limits exceed total budget limit for trip ID {}", tripBudgetLimitDto.getTripId());
            throw new BadRequestException("Total budget category limits cannot exceed total budget limit for the trip");
        }

        if (totalBudgetCategoryLimit > trip.getTotalSpentAmount() && trip.getTotalSpentAmount() > 0) {
            log.error("Total budget category limits exceed total budget for trip ID {}", tripBudgetLimitDto.getTripId());
            throw new BadRequestException("Total budget category limits cannot exceed total budget for the trip");
        }


        // Update fields
        existingLimit.setBudgetCategory(tripBudgetLimitDto.getBudgetCategory());
        existingLimit.setLimitAmount(tripBudgetLimitDto.getLimitAmount());

        // Save updated limit
        TripBudgetCategory updatedLimit = tripBudgetCategoryRepository.save(existingLimit);

        log.info("Trip budget limit updated successfully: {}", updatedLimit);
        return new APIResponse<>(true, "Trip budget limit updated successfully", modelMapper.map(updatedLimit, TripBudgetLimitDto.class));
    }

    @Override
    @Transactional
    public APIResponse<List<TripBudgetLimitDto>> getTripBudgetLimitsByTripId(Long tripId) {
        log.info("Fetching trip budget limits for trip ID: {}", tripId);
        Long touristId = authUtils.loggedInUserId();

        // Validate trip existence
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found with ID: " + tripId));

        // Check if the tourist exists in trip's tourist list
        if (trip.getTourists().stream().noneMatch(t -> t.getUserId().equals(touristId))) {
            log.error("Tourist with ID {} is not part of trip with ID {}", touristId, tripId);
            return new APIResponse<>(false, "Tourist not part of the trip", null);
        }

        // Fetch budget limits for the trip
        List<TripBudgetCategory> budgetLimits = tripBudgetCategoryRepository.findByTripTripId(tripId);

        // Map to DTOs
        List<TripBudgetLimitDto> budgetLimitDtos = budgetLimits.stream()
                .map(limit -> modelMapper.map(limit, TripBudgetLimitDto.class))
                .toList();

        log.info("Fetched {} budget limits for trip ID {}", budgetLimitDtos.size(), tripId);
        return new APIResponse<>(true, "Trip budget limits fetched successfully", budgetLimitDtos);
    }
}
