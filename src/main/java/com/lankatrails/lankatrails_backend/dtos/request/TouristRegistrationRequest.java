package com.lankatrails.lankatrails_backend.dtos.request;

import com.lankatrails.lankatrails_backend.model.enums.UserRole;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TouristRegistrationRequest extends RegistrationRequest{
    @Size(max = 20)
    private String firstName;

    @Size(max = 20)
    private String lastName;

    @Size(max = 50)
    private String country;

    public TouristRegistrationRequest() {
        this.setUserRole(UserRole.PROVIDER);
    }

    public @Size(max = 20) String getFirstName() {
        return firstName;
    }

    public void setFirstName(@Size(max = 20) String firstName) {
        this.firstName = firstName;
    }

    public @Size(max = 20) String getLastName() {
        return lastName;
    }

    public void setLastName(@Size(max = 20) String lastName) {
        this.lastName = lastName;
    }

    public @Size(max = 50) String getCountry() {
        return country;
    }

    public void setCountry(@Size(max = 50) String country) {
        this.country = country;
    }
}
