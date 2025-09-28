package com.lankatrails.lankatrails_backend.model.enums;

import lombok.Getter;

@Getter
public enum ServiceCategory {
    ACCOMMODATION("Accommodation"),
    ACTIVITY("Activity"),
    TOUR_GUIDE("Tour Guide"),
    TRANSPORT("Transport"),
    FOOD_BEVERAGE("Food & Beverage");

    private final String displayName;

    ServiceCategory(String displayName) {
        this.displayName = displayName;
    }
}