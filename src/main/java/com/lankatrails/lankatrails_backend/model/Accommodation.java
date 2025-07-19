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

//    private String about;
    private Integer maxGuests;
    private Integer numberOfRooms;
    private Boolean freeWifi;
    private Boolean parkingAvailable;
    private Boolean breakfastIncluded;
    private Boolean airConditioned;
    private Boolean swimmingPool;
    private Boolean petFriendly;
    private Boolean laundryService;
    private Boolean roomService;
    private Boolean gymAccess;
    private Boolean spaServices;

    @ManyToOne
    @JoinColumn(name = "accommodationCategory_id")
    private AccommodationCategory accommodationCategory;




}
