package com.lankatrails.lankatrails_backend.model.enums;

import lombok.Getter;

@Getter
public enum ActivityType {
    ADVENTURE("Adventure"),
    CULTURAL("Cultural"),
    NATURE("Nature"),
    RELAXATION("Relaxation"),
    SPORTS("Sports"),
    WATER_SPORTS("Water Sports"),
    EDUCATIONAL("Educational"),
    NIGHTLIFE("Nightlife"),
    WELLNESS("Wellness");

    private final String value;

    ActivityType(String value) {
        this.value = value;
    }

    public static ActivityType fromValue(String value) {
        for (ActivityType type : ActivityType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown ActivityType: " + value);
    }
}
