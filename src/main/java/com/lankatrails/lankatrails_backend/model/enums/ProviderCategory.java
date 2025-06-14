package com.lankatrails.lankatrails_backend.model.enums;

import lombok.Getter;

@Getter
public enum ProviderCategory {
    ACCOMMODATION("Accommodation"),
    ACTIVITY("Activity"),
    TOUR_GUIDE("Tour Guide"),
    TRANSPORT("Transport"),
    FOOD_BEVERAGE("Food & Beverage");

    private final String displayName;

    ProviderCategory(String displayName) {
        this.displayName = displayName;
    }
}