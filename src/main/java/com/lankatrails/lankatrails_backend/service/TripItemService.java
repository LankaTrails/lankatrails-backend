package com.lankatrails.lankatrails_backend.service;

import com.lankatrails.lankatrails_backend.dtos.request.TripItemDTO;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;

import java.util.List;

public interface TripItemService {
    APIResponse<TripItemDTO> addTripItem(Long tripId, TripItemDTO tripItemDTO);
}
