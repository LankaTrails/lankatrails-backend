package com.lankatrails.lankatrails_backend.dtos.response;

import com.lankatrails.lankatrails_backend.model.enums.UserRole;
import com.lankatrails.lankatrails_backend.model.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
public abstract class UserProfileDto {
    private Long id;
    private String email;
    private UserRole role;
    private UserStatus status;
//    private String firstName;
//    private String lastName;
//    private String phoneNumber;
//    private String profilePictureUrl;
//    private String bio;
    private boolean emailVerified;
}
