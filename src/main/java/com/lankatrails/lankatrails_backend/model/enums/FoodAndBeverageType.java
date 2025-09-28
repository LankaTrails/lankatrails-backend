package com.lankatrails.lankatrails_backend.model.enums;

import lombok.Getter;

@Getter
public enum FoodAndBeverageType {
    RESTAURANT("Restaurant"),
    BAR("Bar"),
    CAFE("Cafe"),
    FOOD_TRUCK("Food Truck"),
    PUB("Pub"),
    BAKERY("Bakery"),
    BREWERY("Brewery"),
    WINERY("Winery"),
    FOOD_COURT("Food Court"),
    STREET_FOOD("Street Food"),
    DISTILLERY("Distillery"),
    BUFFET("Buffet"),
    ;

    private final String displayName;

    FoodAndBeverageType(String displayName) {
        this.displayName = displayName;
    }
}
