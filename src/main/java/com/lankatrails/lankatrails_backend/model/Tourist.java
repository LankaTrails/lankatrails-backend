package com.lankatrails.lankatrails_backend.model;

import com.lankatrails.lankatrails_backend.model.enums.UserRole;
import com.lankatrails.lankatrails_backend.model.enums.UserStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Set;

@Entity
@Table(name = "tourists")
@Getter
@Setter
@NoArgsConstructor
public class Tourist extends User {
    @Size(max = 20)
    @Column(name = "first_name")
    private String firstName;

    @Size(max = 20)
    @Column(name = "last_name")
    private String lastName;

    @Size(max = 50)
    @Column(name = "country")
    private String country;

    @PrePersist
    protected void onCreate() {
        super.setRole(UserRole.ROLE_TOURIST);
        super.setStatus(UserStatus.ACTIVE);
    }

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "trip_tourists",
            joinColumns = @JoinColumn(name = "tourist_id"),
            inverseJoinColumns = @JoinColumn(name = "trip_id"))
    private Set<Trip> trips;

    public @Size(max = 20) String getFirstName() {
        return firstName;
    }

    public void setFirstName(@Size(max = 20) String firstName) {
        this.firstName = firstName;
    }

    public @Size(max = 50) String getCountry() {
        return country;
    }

    public void setCountry(@Size(max = 50) String country) {
        this.country = country;
    }

    public @Size(max = 20) String getLastName() {
        return lastName;
    }

    public void setLastName(@Size(max = 20) String lastName) {
        this.lastName = lastName;
    }
}
