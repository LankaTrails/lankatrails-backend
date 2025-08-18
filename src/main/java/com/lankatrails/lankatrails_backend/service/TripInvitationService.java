package com.lankatrails.lankatrails_backend.service;

import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.TripResponseDTO;
import io.vavr.API;

public interface TripInvitationService {
    APIResponse<String> createInvitation(Long tripId);
    APIResponse<TripResponseDTO> acceptInvitation(String token);
}
