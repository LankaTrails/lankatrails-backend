package com.lankatrails.lankatrails_backend.controller;

import com.lankatrails.lankatrails_backend.dtos.request.TripInvitationRequest;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.service.TripInvitationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trips/invitations")
@RequiredArgsConstructor
public class TripInvitationController {
    @Autowired
    private TripInvitationService tripInvitationService;

    @PostMapping("/generate")
    public ResponseEntity<APIResponse<String>> createInvitation(@RequestBody TripInvitationRequest tripInvitationRequest) {
        APIResponse<String> response = tripInvitationService.createInvitation(tripInvitationRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{token}/accept")
    public ResponseEntity<APIResponse<String>> acceptInvitation(@PathVariable String token) {
        APIResponse<String> response = tripInvitationService.acceptInvitation(token);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
