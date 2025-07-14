package com.lankatrails.lankatrails_backend.model.enums;

import lombok.Getter;

@Getter
public enum BudgetCategory {
    TRANSPORT("Transport"),
    ACCOMMODATION("Accommodation"),
    FOOD("Food & Drinks"),
    ACTIVITY("Activities & Tours"),
    SHOPPING("Shopping"),
    EMERGENCY("Emergency"),
    COMMUNICATION("Communication"),
    ENTRANCE_FEES("Entrance Fees"),
    ENTERTAINMENT("Entertainment"),
    MISCELLANEOUS("Miscellaneous");

    private final String displayName;

    BudgetCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

