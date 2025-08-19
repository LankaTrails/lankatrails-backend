package com.lankatrails.lankatrails_backend.service.impl;

import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.TripResponseDTO;
import com.lankatrails.lankatrails_backend.exception.BadRequestException;
import com.lankatrails.lankatrails_backend.exception.ResourceNotFoundException;
import com.lankatrails.lankatrails_backend.model.Tourist;
import com.lankatrails.lankatrails_backend.model.Trip;
import com.lankatrails.lankatrails_backend.model.TripInvitation;
import com.lankatrails.lankatrails_backend.model.enums.InvitationStatus;
import com.lankatrails.lankatrails_backend.repositories.TouristRepository;
import com.lankatrails.lankatrails_backend.repositories.TripInvitationRepository;
import com.lankatrails.lankatrails_backend.repositories.TripRepository;
import com.lankatrails.lankatrails_backend.security.utils.AuthUtils;
import com.lankatrails.lankatrails_backend.service.TripInvitationService;
import com.lankatrails.lankatrails_backend.service.TripService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TripInvitationServiceImpl implements TripInvitationService {

    @Autowired
    private final TripInvitationRepository tripInvitationRepository;

    @Autowired
    private final TripRepository tripRepository;

    @Autowired
    private final TripService tripService;

    @Autowired
    private final AuthUtils authUtils;

    @Override
    public APIResponse<String> createInvitation(Long tripId) {
        // Validate trip existence
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip ID: " , tripId));

        if (!trip.getLeadTourist().getUserId().equals(authUtils.loggedInUserId())) {
            throw new BadRequestException("Only the lead tourist can create an invitation for this trip.");
        }

        // Generate a unique token for the invitation
        String token = UUID.randomUUID().toString();

        // Create and save the TripInvitation entity
        TripInvitation invitation = TripInvitation.builder()
                .trip(trip)
                .token(token)
                .status(InvitationStatus.PENDING) // Assuming InvitationStatus is an enum with PENDING status
                .expiresAt(LocalDateTime.now().plusDays(7)) // Set expiration to 7 days from now
                .createdBy(trip.getLeadTourist()) // Replace with actual tourist ID
                .build();
        tripInvitationRepository.save(invitation);

        return new APIResponse<>(
                true,
                "Invitation created successfully",
                token
        );
    }

    @Override
    @Transactional
    public APIResponse<TripResponseDTO> acceptInvitation(String token) {
        // Validate the invitation token
        TripInvitation invitation = tripInvitationRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation with token: ", token));

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new BadRequestException("This invitation is no longer valid.");
        }

        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("This invitation has expired.");
        }

        // Add the tourist to the trip
        APIResponse<TripResponseDTO> response = tripService.addTouristToTrip(invitation.getTrip().getTripId(), authUtils.loggedInUserId());

        if (!response.isSuccess()) {
            throw new BadRequestException("Failed to accept the invitation: " + response.getMessage());
        }

        // Update the invitation status to ACCEPTED
        invitation.setStatus(InvitationStatus.ACCEPTED);
        tripInvitationRepository.save(invitation);

        return response;
    }
}
