package com.lankatrails.lankatrails_backend.model.enums;

import lombok.Getter;

@Getter
public enum FuelType {
    PETROL("Petrol"),
    DIESEL("Diesel"),
    ELECTRIC("Electric"),
    HYBRID("Hybrid"),
    CNG("CNG"),
    LPG("LPG");

    private final String value;

    FuelType(String value) {
        this.value = value;
    }

}
