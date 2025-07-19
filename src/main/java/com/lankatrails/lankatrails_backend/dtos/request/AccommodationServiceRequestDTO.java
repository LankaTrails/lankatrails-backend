package com.lankatrails.lankatrails_backend.dtos.request;

import com.lankatrails.lankatrails_backend.model.enums.AccommodationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccommodationServiceRequestDTO extends ServiceRequest{
    private String about;
    private AccommodationType accommodationType;
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

}
