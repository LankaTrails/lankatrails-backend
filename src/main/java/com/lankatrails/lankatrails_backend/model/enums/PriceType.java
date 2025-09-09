package com.lankatrails.lankatrails_backend.model.enums;

import lombok.Getter;

@Getter
public enum PriceType {
    FIXED("Fixed"),               // Fixed price for the booking
    PER_PERSON("Per Person"),     // Price per person
    PER_UNIT("Per Unit"),         // Price per room/vehicle/unit
    HYBRID("Hybrid"),             // Base price + per person/unit
    PER_HOUR("Per Hour"),         // Price per hour
    PER_DAY("Per Day"),           // Price per day
    PER_NIGHT("Per Night"),       // Price per night
    PER_KM("Per KM");             // Price per kilometer

    private final String displayName;

    PriceType(String displayName) {
        this.displayName = displayName;
    }

}
