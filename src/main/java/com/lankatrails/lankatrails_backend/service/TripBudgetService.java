package com.lankatrails.lankatrails_backend.service;

import com.lankatrails.lankatrails_backend.dtos.request.TripBudgetLimitDto;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;

import java.util.List;

public interface TripBudgetService {
    APIResponse<TripBudgetLimitDto> addTripBudgetLimit(TripBudgetLimitDto tripBudgetLimitDto);
    APIResponse<TripBudgetLimitDto> updateTripBudgetLimit(TripBudgetLimitDto tripBudgetLimitDto);
//    APIResponse<Void> deleteTripBudgetLimit(Long limitId);
//    APIResponse<List<TripBudgetLimitDto>> getTripBudgetLimitsByTripId(Long tripId);
}
