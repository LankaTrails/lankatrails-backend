package com.lankatrails.lankatrails_backend.model.enums;

import lombok.Getter;

import java.util.Set;

@Getter
public enum TripTagType {
    HONEYMOON("Honeymoon"),
    FAMILY("Family"),
    ADVENTURE("Adventure"),
    CULTURAL("Cultural"),
    BUSINESS("Business"),
    EDUCATIONAL("Educational"),
    RELAXATION("Relaxation"),
    SOLO("Solo"),
    GROUP("Group"),
    ROMANTIC("Romantic"),
    NATURE("Nature"),
    HISTORICAL("Historical"),
    SPORTS("Sports"),
    FESTIVAL("Festival"),
    LEISURE("Leisure");

    private final String displayName;

    TripTagType(String displayName) {
        this.displayName = displayName;
    }

    public Set<TripTagType> getAllTags() {
        return Set.of(values());
    }
}
