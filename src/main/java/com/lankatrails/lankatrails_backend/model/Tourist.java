package com.lankatrails.lankatrails_backend.model;

import com.lankatrails.lankatrails_backend.model.enums.UserRole;
import com.lankatrails.lankatrails_backend.model.enums.UserStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
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

    @Size(max = 15)
    @Column(name = "phone_number")
    private String phoneNumber;

    @PrePersist
    protected void onCreate() {
        super.setRole(UserRole.ROLE_TOURIST);
        super.setStatus(UserStatus.ACTIVE);
    }

    @OneToMany(mappedBy = "tourist", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<TripParticipant> tripParticipants;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "tourist_favourite_services",
            joinColumns = @JoinColumn(name = "tourist_id"),
            inverseJoinColumns = @JoinColumn(name = "service_id"))
    private Set<Service> favouriteServices;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "tourist_favourite_places",
            joinColumns = @JoinColumn(name = "tourist_id"),
            inverseJoinColumns = @JoinColumn(name = "place_id"))
    private Set<Place> favouritePlaces;

    @OneToMany(mappedBy = "tourist")
    private List<Booking> bookings = new ArrayList<>();

}
