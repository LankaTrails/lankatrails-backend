package com.lankatrails.lankatrails_backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "accommodation")
@Getter
@Setter
@NoArgsConstructor
public class Accommodation extends Service {

    @Column(name = "free_wifi")
    private Boolean freeWifi;

    @Column(name = "parking_available")
    private Boolean parkingAvailable;

    @Column(name = "breakfast_included")
    private Boolean breakfastIncluded;

    @Column(name = "air_conditioned")
    private Boolean airConditioned;

    @Column(name = "swimming_pool")
    private Boolean swimmingPool;

    @Column(name = "pet_friendly")
    private Boolean petFriendly;

    @Column(name = "laundry_service")
    private Boolean laundryService;

    @Column(name = "room_service")
    private Boolean roomService;

    @Column(name = "gym_access")
    private Boolean gymAccess;

    @Column(name = "spa_services")
    private Boolean spaServices;

    @ManyToOne
    @JoinColumn(name = "accommodationCategory_id")
    private AccommodationCategory accommodationCategory;

}
