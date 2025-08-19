package com.lankatrails.lankatrails_backend.service.impl;

import com.lankatrails.lankatrails_backend.dtos.request.TripBudgetLimitDto;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.exception.BadRequestException;
import com.lankatrails.lankatrails_backend.model.Trip;
import com.lankatrails.lankatrails_backend.model.TripBudgetCategoryLimit;
import com.lankatrails.lankatrails_backend.repositories.TouristRepository;
import com.lankatrails.lankatrails_backend.repositories.TripBudgetCategoryLimitRepository;
import com.lankatrails.lankatrails_backend.repositories.TripExpenseRepository;
import com.lankatrails.lankatrails_backend.repositories.TripRepository;
import com.lankatrails.lankatrails_backend.security.utils.AuthUtils;
import com.lankatrails.lankatrails_backend.service.TripBudgetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private TripBudgetCategoryLimitRepository tripBudgetCategoryLimitRepository;

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

        // Check if a limit already exists for the given category
        if (trip.getTripBudgetCategoryLimits().stream()
                .anyMatch(limit -> limit.getBudgetCategory() == tripBudgetLimitDto.getBudgetCategory())) {
            log.error("Budget limit for category {} already exists for trip ID {}",
                      tripBudgetLimitDto.getBudgetCategory(), tripBudgetLimitDto.getTripId());
            throw new BadRequestException("Budget limit for this category already exists");
        }

        // Map DTO to entity and save
        TripBudgetCategoryLimit tripBudgetCategoryLimit = modelMapper.map(tripBudgetLimitDto, TripBudgetCategoryLimit.class);
        tripBudgetCategoryLimit.setTrip(trip);
        TripBudgetCategoryLimit savedLimit = tripBudgetCategoryLimitRepository.save(tripBudgetCategoryLimit);

        log.info("Trip budget limit added successfully: {}", savedLimit);
        return new APIResponse<>(true, "Trip budget limit added successfully", modelMapper.map(savedLimit, TripBudgetLimitDto.class));
    }
}
