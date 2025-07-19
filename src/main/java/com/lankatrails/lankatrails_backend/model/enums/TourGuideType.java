package com.lankatrails.lankatrails_backend.model.enums;

import lombok.Getter;

@Getter
public enum TourGuideType {
    NATIONAL("National"),
    CHAUFFEUR("Chauffeur"),
    SITE("Site"),
    AREA("Area");

    private final String value;

    TourGuideType(String value) {
        this.value = value;
    }

    private static TourGuideType fromValue(String value) {
        for (TourGuideType type : TourGuideType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown TourGuideType: " + value);
    }
}
