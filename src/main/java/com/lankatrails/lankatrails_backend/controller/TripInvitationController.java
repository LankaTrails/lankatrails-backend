package com.lankatrails.lankatrails_backend.controller;

import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.TripResponseDTO;
import com.lankatrails.lankatrails_backend.service.TripInvitationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trips/invitations")
@RequiredArgsConstructor
public class TripInvitationController {
    @Autowired
    private TripInvitationService tripInvitationService;

    @PostMapping("/{tripId}/generate")
    public ResponseEntity<APIResponse<String>> createInvitation(@PathVariable Long tripId) {
        APIResponse<String> response = tripInvitationService.createInvitation(tripId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{token}/accept")
    public ResponseEntity<APIResponse<TripResponseDTO>> acceptInvitation(@PathVariable String token) {
        APIResponse<TripResponseDTO> response = tripInvitationService.acceptInvitation(token);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
