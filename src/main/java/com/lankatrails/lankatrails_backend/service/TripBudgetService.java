package com.lankatrails.lankatrails_backend.service;

import com.lankatrails.lankatrails_backend.dtos.TripBudgetCategoryDto;
import com.lankatrails.lankatrails_backend.dtos.TripBudgetDto;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.model.Trip;
import com.lankatrails.lankatrails_backend.model.enums.BudgetCategory;

import java.util.List;

public interface TripBudgetService {
    APIResponse<TripBudgetCategoryDto> addTripBudgetLimit(TripBudgetCategoryDto tripBudgetCategoryDto);
//    APIResponse<TripBudgetCategoryDto> updateTripBudgetLimit(TripBudgetCategoryDto tripBudgetCategoryDto);
    APIResponse<List<TripBudgetCategoryDto>> getTripBudgetLimitsByTripId(Long tripId);
    APIResponse<TripBudgetDto> getTripBudgetDetails(Long tripId);
    APIResponse<TripBudgetDto> updateTripTotalBudget(TripBudgetDto tripBudgetDto);
    void updateTripBudgetSpentAmount(Trip trip, Double spentAmount, BudgetCategory budgetCategory);
}
