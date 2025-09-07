package com.lankatrails.lankatrails_backend.dtos.request;

import com.lankatrails.lankatrails_backend.model.enums.TripRole;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TripInvitationRequest {
    private Long tripId;
    private TripRole role;
    private Boolean isGroupInvitation = false; // Default to false if not specified
}
