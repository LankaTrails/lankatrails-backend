package com.lankatrails.lankatrails_backend.service;

import com.lankatrails.lankatrails_backend.dtos.request.TripItemDTO;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface TripItemService {
    APIResponse<String> addTripItem(Long tripId, TripItemDTO tripItemDTO);

    Boolean hasOverlappingTripItems(Long tripId, LocalDateTime startDateTime, LocalDateTime endDateTime);
}
