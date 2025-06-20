package com.lankatrails.lankatrails_backend.dtos.response;

import com.lankatrails.lankatrails_backend.model.enums.UserRole;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LoginResponse {
    private Long id;
    private String jwtToken;
    private String email;
    private UserRole role;
    private List<String> categories;

    public LoginResponse(Long id, String email, UserRole role, String jwtToken, List<String> categories) {
        this.id = id;
        this.email = email;
        this.role = role;
        this.jwtToken = jwtToken;
        this.categories = categories;
    }

    public LoginResponse(Long id, String email, UserRole role, String jwtToken) {
        this.id = id;
        this.email = email;
        this.role = role;
        this.jwtToken = jwtToken;
    }
}
