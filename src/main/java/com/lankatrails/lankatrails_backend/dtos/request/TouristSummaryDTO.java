package com.lankatrails.lankatrails_backend.dtos.request;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TouristSummaryDTO {
    private Long id;           // tourist_id
    private String fullName;   // firstName + lastName
    private String email;      // inherited from User
    private String profilePicture;
}
