package com.lankatrails.lankatrails_backend.model.enums;


import lombok.Getter;

@Getter
public enum UserRole {
    ROLE_TOURIST("Tourist"),
    ROLE_PROVIDER("Service Provider"),
    ROLE_ADMIN("Administrator");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

}