package com.lankatrails.lankatrails_backend.model.enums;

import lombok.Getter;

@Getter
public enum PriceType {
    FIXED("Fixed"),
    PER_PERSON("Per Person"),
    PER_GROUP("Per Group"),
    PER_KM("Per KM"),
    PER_HOUR("Per Hour"),
    PER_DAY("Per Day"),
    PER_NIGHT("Per Night"),
    PER_WEEK("Per Week"),
    PER_MONTH("Per Month");

    private final String value;

    PriceType(String value) {
        this.value = value;
    }

    public static PriceType fromValue(String value) {
        for (PriceType type : PriceType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown PriceType: " + value);
    }

}
