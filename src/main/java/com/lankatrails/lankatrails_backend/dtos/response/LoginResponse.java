package com.lankatrails.lankatrails_backend.dtos.response;

import com.lankatrails.lankatrails_backend.model.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class LoginResponse {
    private Long id;
    private String jwtToken;
    private String email;
    private UserRole role;
    private String refreshToken;
    private boolean emailVerified;


    public LoginResponse(Long id, String email, UserRole role, String jwtToken, boolean emailVerified) {
        this.id = id;
        this.email = email;
        this.role = role;
        this.jwtToken = jwtToken;
        this.emailVerified = emailVerified;
    }


    public LoginResponse(Long id, String email, UserRole role, String jwtToken, boolean emailVerified, String refreshToken) {
        this.id = id;
        this.email = email;
        this.role = role;
        this.jwtToken = jwtToken;
        this.emailVerified = emailVerified;
        this.refreshToken = refreshToken;
    }
}
