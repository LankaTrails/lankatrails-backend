package com.lankatrails.lankatrails_backend.dtos.response;

import com.lankatrails.lankatrails_backend.model.enums.UserRole;
import com.lankatrails.lankatrails_backend.model.enums.UserStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public class TouristProfileDto extends UserProfileDto {
    private String firstName;
    private String lastName;
    private String country;
//    private String phoneNumber;
//    private String profilePictureUrl;
//    private String bio;

    public TouristProfileDto(Long id, String email, UserRole role, UserStatus status, boolean emailVerified) {
        super(id, email, role, status, emailVerified);
    }
}
