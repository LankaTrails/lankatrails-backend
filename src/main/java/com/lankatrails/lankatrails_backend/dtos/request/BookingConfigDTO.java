package com.lankatrails.lankatrails_backend.dtos.request;

import com.lankatrails.lankatrails_backend.model.enums.BookingType;
import lombok.*;

import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingConfigDTO {
    private BookingType bookingType;
    // Capacity and unit management
    private Integer totalUnits;
    private Integer unitAdultCapacity;
    private Integer unitChildCapacity;
    private Integer minUnitsPerBooking;
    private Integer maxUnitsPerBooking;
    private Boolean allowExtraCapacity;
    private Integer extraAdultCapacity;
    private Integer extraChildCapacity;
    private Integer extraAdultCapacityLimit;
    private Integer extraChildCapacityLimit;
    //for time-based bookings
    private Integer slotDuration; // in minutes
    private Integer bufferTime;   // in minutes
    private Boolean allowBackToBackBookings;
    // for date-based bookings
    private Integer minimumBookingDays;
    private Integer maximumBookingDays;
    private LocalTime defaultCheckInTime; // format HH:mm
    private LocalTime defaultCheckOutTime; // format HH:mm
    // common fields
    private Integer advanceBookingPeriod; // in days
    private Integer lastMinuteBookingPeriod; // in hours
}
