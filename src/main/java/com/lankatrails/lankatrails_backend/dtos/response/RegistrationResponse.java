package com.lankatrails.lankatrails_backend.dtos.response;

import com.lankatrails.lankatrails_backend.model.enums.UserRole;
import com.lankatrails.lankatrails_backend.model.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationResponse {
    private Long userId;
    private String email;
    private UserRole role;
    private UserStatus status;
    private Boolean emailVerified;
}