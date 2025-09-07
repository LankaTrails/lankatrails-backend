package com.lankatrails.lankatrails_backend.model;

import com.lankatrails.lankatrails_backend.model.enums.BookingType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Entity
@Getter
@Setter
@Table(name = "booking_configurations")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingConfiguration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingConfigId;

    @Column(name = "booking_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private BookingType bookingType;

    @OneToOne(mappedBy = "bookingConfiguration", fetch = FetchType.LAZY)
    private Service service;

    // Capacity and unit management

    @Column(name = "total_units")
    private Integer totalUnits;

    @Column(name = "require_child_info")
    private Boolean requireChildInfo;

    @Column(name = "manage_capacity")
    private Boolean manageCapacity;

    @Column(name = "unit_adult_capacity")
    private Integer unitAdultCapacity;

    @Column(name = "unit_child_capacity")
    private Integer unitChildCapacity;

    @Column(name = "min_units_per_booking")
    private Integer minUnitsPerBooking;

    @Column(name = "max_units_per_booking")
    private Integer maxUnitsPerBooking;

    @Column(name = "allow_extra_capacity")
    private Boolean allowExtraCapacity;

    @Column(name = "extra_adult_capacity")
    private Integer extraAdultCapacity;

    @Column(name = "extra_child_capacity")
    private Integer extraChildCapacity;

    @Column(name = "extra_adult_capacity_limit")
    private Integer extraAdultCapacityLimit;

    @Column(name = "extra_child_capacity_limit")
    private Integer extraChildCapacityLimit;

    //for time-based bookings

    @Column(name = "slot_duration")
    private Integer slotDuration; // in minutes

    @Column(name = "buffer_time")
    private Integer bufferTime; // in minutes

    @Column(name = "allow_back_to_back_bookings")
    private Boolean allowBackToBackBookings;

    //for day-based bookings

    @Column(name = "minimum_booking_days")
    private Integer minimumBookingDays;

    @Column(name = "maximum_booking_days")
    private Integer maximumBookingDays;

    @Column(name = "default_check_in_time")
    private LocalTime defaultCheckInTime;

    @Column(name = "default_check_out_time")
    private LocalTime defaultCheckOutTime;

    // common fields

    @Column(name = "advance_booking_period")
    private Integer advanceBookingPeriod; // in days

    @Column(name = "last_minute_booking_period")
    private Integer lastMinuteBookingPeriod; // in hours


}
