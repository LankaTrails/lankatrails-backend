package com.lankatrails.lankatrails_backend.model.enums;

import lombok.Getter;

@Getter
public enum TransmissionType {
    MANUAL("Manual"),
    AUTOMATIC("Automatic"),
    SEMI_AUTOMATIC("Semi-Automatic");

    private final String value;

    TransmissionType(String value) {
        this.value = value;
    }

}
