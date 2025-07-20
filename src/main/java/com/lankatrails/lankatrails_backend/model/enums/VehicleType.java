package com.lankatrails.lankatrails_backend.model.enums;

import lombok.Getter;

@Getter
public enum VehicleType {
    CAR("Car"),
    VAN("Van"),
    BUS("Bus"),
    TRUCK("Truck"),
    MOTORCYCLE("Motorcycle"),
    BICYCLE("Bicycle"),
    SCOOTER("Scooter"),
    PICKUP("Pickup Truck"),
    SUV("SUV"),
    TUK_TUK("Tuk Tuk");

    private final String value;

    VehicleType(String value) {
        this.value = value;
    }

    public static VehicleType fromValue(String value) {
        for (VehicleType type : VehicleType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown VehicleType: " + value);
    }
}
