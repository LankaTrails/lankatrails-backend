package com.lankatrails.lankatrails_backend.dtos.request;


import com.lankatrails.lankatrails_backend.model.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class RegistrationRequest {
//    @ValidEmail
    @NotBlank
    @Email
    @Size(max = 50)
    private String email;

    @NotBlank

    @Size(min = 8, max = 30)
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-zA-Z]).{8,}$",
            message = "Password must contain letters and numbers.")
    private String password;

    @Setter
    @Getter
    private UserRole userRole;

    public @NotBlank @Size(max = 50) String getEmail() {
        return email;
    }

    public void setEmail(@NotBlank @Size(max = 50) String email) {
        this.email = email;
    }

    public @NotBlank @Size(min = 8, max = 30) String getPassword() {
        return password;
    }

    public void setPassword(@NotBlank @Size(min = 8, max = 30) String password) {
        this.password = password;
    }

}