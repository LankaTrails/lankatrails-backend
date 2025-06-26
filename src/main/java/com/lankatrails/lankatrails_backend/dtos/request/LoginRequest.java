package com.lankatrails.lankatrails_backend.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LoginRequest {

    @NotBlank
    private String email;

    @NotBlank
    private String password;

}
