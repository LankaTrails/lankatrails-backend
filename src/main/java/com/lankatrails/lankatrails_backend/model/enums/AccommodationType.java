package com.lankatrails.lankatrails_backend.model.enums;

import lombok.Getter;

@Getter
public enum AccommodationType {
    HOTEL("Hotel"),
    GUEST_HOUSE("Guest House"),
    HOSTEL("Hostel"),
    VILLA("Villa"),
    APARTMENT("Apartment"),
    RESORT("Resort"),
    HOMESTAY("Homestay"),
    CAMPING("Camping"),
    LODGE("Lodge");

    private final String value;

    AccommodationType(String value) {
        this.value = value;
    }

    public static AccommodationType fromValue(String value) {
        for (AccommodationType type : AccommodationType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown AccommodationType: " + value);
    }
}
