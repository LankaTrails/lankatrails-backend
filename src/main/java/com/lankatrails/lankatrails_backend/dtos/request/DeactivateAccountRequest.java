package com.lankatrails.lankatrails_backend.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DeactivateAccountRequest {

    @NotBlank(message = "Password is required")
    private String password;

    @Size(max = 500)
    private String reason;
}
