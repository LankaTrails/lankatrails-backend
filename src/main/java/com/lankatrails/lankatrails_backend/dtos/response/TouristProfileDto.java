package com.lankatrails.lankatrails_backend.dtos.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class TouristProfileDto extends UserProfileDto {
    private String firstName;
    private String lastName;
    private String country;
    private String phoneNumber;
//    private String profilePictureUrl;
//    private String bio;

}
