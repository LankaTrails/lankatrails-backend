package com.lankatrails.lankatrails_backend.model.enums;

import lombok.Getter;

@Getter
public enum UserStatus {
    ACTIVE("Active"),
    PENDING("Pending"),
    DISABLED("Disabled"),;

    private final String displayName;

    UserStatus(String displayName) {
        this.displayName = displayName;
    }
}


