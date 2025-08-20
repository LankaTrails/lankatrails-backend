package com.lankatrails.lankatrails_backend.service.impl;

import com.lankatrails.lankatrails_backend.dtos.ChatRoomDto;
import com.lankatrails.lankatrails_backend.dtos.request.TripInvitationRequest;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.TripResponseDTO;
import com.lankatrails.lankatrails_backend.exception.BadRequestException;
import com.lankatrails.lankatrails_backend.exception.ResourceNotFoundException;
import com.lankatrails.lankatrails_backend.exception.UserNotFoundException;
import com.lankatrails.lankatrails_backend.model.Tourist;
import com.lankatrails.lankatrails_backend.model.Trip;
import com.lankatrails.lankatrails_backend.model.TripInvitation;
import com.lankatrails.lankatrails_backend.model.TripParticipant;
import com.lankatrails.lankatrails_backend.model.enums.InvitationStatus;
import com.lankatrails.lankatrails_backend.model.enums.TripPrivilege;
import com.lankatrails.lankatrails_backend.model.enums.TripRole;
import com.lankatrails.lankatrails_backend.repositories.TouristRepository;
import com.lankatrails.lankatrails_backend.repositories.TripInvitationRepository;
import com.lankatrails.lankatrails_backend.repositories.TripRepository;
import com.lankatrails.lankatrails_backend.security.utils.AuthUtils;
import com.lankatrails.lankatrails_backend.service.ChatRoomService;
import com.lankatrails.lankatrails_backend.service.TripInvitationService;
import com.lankatrails.lankatrails_backend.service.TripService;
import com.lankatrails.lankatrails_backend.service.utils.TripPrivilegeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TripInvitationServiceImpl implements TripInvitationService {

    @Autowired
    private final TripInvitationRepository tripInvitationRepository;

    @Autowired
    private final TripRepository tripRepository;

    @Autowired
    private final TripService tripService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private AuthUtils authUtils;

    @Autowired
    private TouristRepository touristRepository;

    @Autowired
    private ChatRoomService chatRoomService;

    @Autowired
    private final TripPrivilegeUtils tripPrivilegeUtils;

    @Override
    @Transactional
    public APIResponse<String> createInvitation(TripInvitationRequest tripInvitationRequest) {
        // Validate trip existence
        Trip trip = tripRepository.findById(tripInvitationRequest.getTripId())
                .orElseThrow(() -> new ResourceNotFoundException("Trip ID: " , tripInvitationRequest.getTripId()));

        //Get the logged-in user Trip Role
        TripParticipant loggedInParticipant = trip.getParticipants().stream()
                .filter(participant -> participant.getTourist().getUserId().equals(authUtils.loggedInUserId()))
                .findFirst()
                .orElseThrow(() -> new UserNotFoundException("Logged-in user is not a participant of this trip"));

        // Validate participant has permission to remove tourist
        if (!tripPrivilegeUtils.hasPrivilege(loggedInParticipant.getTripRole(), TripPrivilege.INVITE_MEMBERS)) {
            throw new BadRequestException("Only admins can remove tourists from the trip");
        }

        // Generate a unique token for the invitation
        String token = UUID.randomUUID().toString();

        // Create and save the TripInvitation entity
        TripInvitation invitation = TripInvitation.builder()
                .trip(trip)
                .token(token)
                .status(InvitationStatus.PENDING)
                .role(tripInvitationRequest.getRole()) // Set the role from the request
                .isGroupInvitation(tripInvitationRequest.getIsGroupInvitation())
                .createdBy(loggedInParticipant.getTourist()) // Replace with actual tourist ID
                .isExpired(false) // Initially not expired
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(7)) // Set expiration to 7 days from now
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
    public APIResponse<String> acceptInvitation(String token) {
        // Validate the invitation token
        TripInvitation invitation = tripInvitationRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation with token: ", token));

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new BadRequestException("This invitation is no longer valid.");
        }

        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("This invitation has expired.");
        }

        Long touristId = authUtils.loggedInUserId();
        Tourist tourist = touristRepository.findById(touristId)
                .orElseThrow(() -> new UserNotFoundException("Tourist not found with id: " + touristId));

        Trip trip = invitation.getTrip();

        // Check if the tourist already in trip
        if (trip.getParticipants().stream()
                .anyMatch(p -> p.getTourist().getUserId().equals(touristId))) {
            throw new BadRequestException("Tourist is already part of this trip");
        }

        List<Trip> overlappingTrips = tripRepository.findOverlappingTripsForTourist(
                tourist, trip.getStartDate(), trip.getEndDate());
        if (!overlappingTrips.isEmpty()) {
            throw new BadRequestException("Tourist has overlapping trips during this period");
        }

        // Add the tourist to the trip
        TripParticipant participant = TripParticipant.builder()
                .tourist(tourist)
                .tripRole(TripRole.MEMBER)
                .joinedAt(LocalDateTime.now())
                .invitedBy(invitation.getCreatedBy())
                .trip(trip)
                .build();
        participant.setPrivileges(tripPrivilegeUtils.getDefaultPrivileges(TripRole.MEMBER));
        trip.getParticipants().add(participant);

        Trip updatedTrip = tripRepository.save(trip);

        // Update chat room for the trip
        ChatRoomDto chatRoomDto = chatRoomService.setChatRoomForTrip(trip);

        // Update invitation status
        if (!invitation.getIsGroupInvitation()) {
            invitation.setStatus(InvitationStatus.EXPIRED);
            invitation.setIsExpired(true);
            tripInvitationRepository.save(invitation);
        }


        return new APIResponse<>(
                true,
                "Invitation accepted successfully",
                "You have been added to the trip: " + updatedTrip.getTripName()
        );
    }

}
