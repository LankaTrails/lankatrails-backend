package com.lankatrails.lankatrails_backend.model.enums;


import lombok.Getter;

@Getter
public enum UserRole {
    TOURIST("Tourist"),
    PROVIDER("Service Provider"),
    ADMIN("Administrator");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

}