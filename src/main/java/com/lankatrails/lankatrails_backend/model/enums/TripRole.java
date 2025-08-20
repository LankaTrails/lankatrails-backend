package com.lankatrails.lankatrails_backend.model.enums;

import lombok.Getter;

@Getter
public enum TripRole {
    ADMIN("Admin"),
    MEMBER("Member"),
    EDITOR("Editor"),
    VIEWER("Viewer");

    private final String displayName;

    TripRole(String displayName) {
        this.displayName = displayName;
    }
}
