package com.lankatrails.lankatrails_backend.model.enums;

public enum TripPrivilege {
    // Trip Management
    EDIT_TRIP_DETAILS,
    DELETE_TRIP,

    // Itinerary & Trip Items
    ADD_TRIP_ITEMS,
    EDIT_TRIP_ITEMS,
    DELETE_TRIP_ITEMS,

    // Budget & Financial
    SET_BUDGET_LIMITS,
    ADD_EXPENSES,
    EDIT_EXPENSES,
    DELETE_EXPENSES,

    // Member Management
    INVITE_MEMBERS,
    REMOVE_MEMBERS,
    MANAGE_ROLES,

    // Communication
    SEND_MESSAGES,
    DELETE_MESSAGES,
    MANAGE_PARTICIPANTS,

    // Booking & Reservation
    ADD_BOOKINGS,
    CANCEL_BOOKINGS,

}