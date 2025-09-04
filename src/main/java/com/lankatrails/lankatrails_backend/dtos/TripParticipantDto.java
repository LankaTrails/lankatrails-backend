package com.lankatrails.lankatrails_backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TripParticipantDto {
    private Long participantId;
    private String firstName;
    private String lastName;
    private String profileImageUrl;
    private String role;
}
