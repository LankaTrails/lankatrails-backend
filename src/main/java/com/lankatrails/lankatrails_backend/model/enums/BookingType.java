package com.lankatrails.lankatrails_backend.model.enums;

import lombok.Getter;

@Getter
public enum BookingType {
    TIME_SLOTS("Bookings are made for specific time slots. Suitable for appointments, tours, or rentals that require precise timing."),
    MULTI_DAY("Bookings can span multiple days. Ideal for accommodations, events, or services that last several days."),
    WHOLE_DAY("Bookings are for the entire day, regardless of specific hours. Common for day-long events or rentals."),
    FIXED_TIME("Bookings are made for a fixed duration, such as hourly or half-day slots. Useful for services like consultations or equipment rentals."),
    FLEXIBLE_HOURS("Bookings allow customers to choose start and end times within a broader availability window. Suitable for services that offer flexibility."),
    EVENT_BASED("Bookings are tied to specific events or occasions, such as weddings, conferences, or festivals."),;

    private final String description;

    BookingType(String description) {
        this.description = description;
    }
}
