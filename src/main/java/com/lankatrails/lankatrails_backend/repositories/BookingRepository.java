package com.lankatrails.lankatrails_backend.repositories;

import com.lankatrails.lankatrails_backend.model.Booking;
import com.lankatrails.lankatrails_backend.model.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByStartDateTimeAndEndDateTimeAndTripItem_Service_ServiceIdAndBookingStatus(
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            Long serviceId,
            BookingStatus bookingStatus
    );

    List<Booking> findByStartDateTimeAndEndDateTimeAndTripParticipant_Tourist_UserIdAndBookingStatus(
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            Long userId,
            BookingStatus bookingStatus
    );

    Optional<Booking> findByStartDateTimeAndTripItem_Service_ServiceIdAndBookingStatus(
            LocalDateTime startDateTime,
            Long serviceId,
            BookingStatus bookingStatus
    );

    @Query("SELECT b FROM Booking b WHERE " +
            "b.tripItem.service.serviceId = :serviceId AND " +
            "(b.startDateTime < :endDateTime AND b.endDateTime > :startDateTime) AND " +
            "b.bookingStatus = :bookingStatus")
    List<Booking> findExactConflictingBookings(
            @Param("serviceId") Long serviceId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime,
            @Param("bookingStatus") BookingStatus bookingStatus
    );

    @Query("SELECT b FROM Booking b WHERE " +
            "b.tripParticipant.tourist.userId = :userId AND " +
            "(b.startDateTime < :endDateTime AND b.endDateTime > :startDateTime) AND " +
            "b.bookingStatus = :bookingStatus")
    List<Booking> findOverlappingBookings(
            @Param("userId") Long userId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime,
            @Param("bookingStatus") BookingStatus bookingStatus
    );

    @Query("SELECT b FROM Booking b WHERE " +
            "DATE(b.startDateTime) = :date AND " +
            "b.tripItem.service.serviceId = :serviceId AND " +
            "b.bookingStatus = :bookingStatus")
    List<Booking> findBookingsOnADay(
            @Param("date") LocalDate date,
            @Param("serviceId") Long serviceId,
            @Param("bookingStatus") BookingStatus bookingStatus
    );

    // Query to find already booked units for a service using standard interval overlap logic
    @Query("SELECT COALESCE(SUM(ti.noOfUnits), 0) " +
            "FROM Booking b " +
            "JOIN b.tripItem ti " +
            "WHERE ti.service.serviceId = :serviceId " +
            "AND b.bookingStatus = :status " +
            "AND (b.startDateTime < :end AND b.endDateTime > :start)")
    Integer findBookedUnitsDuringPeriod(@Param("serviceId") Long serviceId,
                                        @Param("start") LocalDateTime start,
                                        @Param("end") LocalDateTime end,
                                        @Param("status") BookingStatus status);

    // Optimization for TIME_SLOTS bookings - check exact time matches
    @Query("SELECT COUNT(b) > 0 FROM Booking b " +
            "WHERE b.tripItem.service.serviceId = :serviceId " +
            "AND b.startDateTime = :startDateTime " +
            "AND b.endDateTime = :endDateTime " +
            "AND b.bookingStatus = :bookingStatus")
    boolean existsExactTimeSlotBooking(@Param("serviceId") Long serviceId,
                                       @Param("startDateTime") LocalDateTime startDateTime,
                                       @Param("endDateTime") LocalDateTime endDateTime,
                                       @Param("bookingStatus") BookingStatus bookingStatus);

//    List<Booking> findByService_ServiceIdAndTourist_UserId(Long serviceId, Long touristId);

}
