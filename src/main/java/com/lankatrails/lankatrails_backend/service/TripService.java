package com.lankatrails.lankatrails_backend.service;

import com.lankatrails.lankatrails_backend.dtos.request.ExpenseDTO;
import com.lankatrails.lankatrails_backend.dtos.request.TripItemDTO;
import com.lankatrails.lankatrails_backend.dtos.request.TripRequestDTO;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.ExpenseResponseDTO;
import com.lankatrails.lankatrails_backend.dtos.response.TripResponseDTO;

import java.util.List;

public interface TripService {
    APIResponse<TripResponseDTO> createTrip(TripRequestDTO tripRequestDTO);

    APIResponse<List<TripResponseDTO>> getAllMyTrips();

    APIResponse<TripResponseDTO> getTripById(Long tripId);

    APIResponse<List<TripItemDTO>> getTripItemsByTripId(Long tripId);
    
    APIResponse<ExpenseResponseDTO> createExpense(ExpenseDTO expenseDTO);
    
    APIResponse<List<ExpenseResponseDTO>> getExpensesByTripId(Long tripId);
}
