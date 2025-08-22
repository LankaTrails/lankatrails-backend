package com.lankatrails.lankatrails_backend.repositories;

import com.lankatrails.lankatrails_backend.model.Booking;
import com.lankatrails.lankatrails_backend.model.Tourist;
import com.lankatrails.lankatrails_backend.model.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking,Long> {
    List<Booking> findByStartTimeAndEndTimeAndFromDateAndToDateAndService_ServiceIdAndBookingStatus(
            LocalTime startTime,
            LocalTime endTime,
            LocalDate fromDate,
            LocalDate endDate,
            Long serviceId,
            BookingStatus bookingStatus
    );
    List<Booking> findByStartTimeAndEndTimeAndFromDateAndToDateAndTourist_UserIdAndBookingStatus(
            LocalTime startTime,
            LocalTime endTime,
            LocalDate fromDate,
            LocalDate toDate,
            Long userId,
            BookingStatus bookingStatus
    );
    Optional<Booking> findByFromDateAndService_ServiceIdAndBookingStatus(LocalDate fromDate, Long id,BookingStatus bookingStatus);

    @Query("SELECT b FROM Booking b WHERE " +
            "b.service.id = :serviceId AND " +
            "((b.fromDate < :toDate OR (b.fromDate = :toDate AND b.startTime <= :endTime)) AND " +
            "(b.toDate > :fromDate OR (b.toDate = :fromDate AND b.endTime >= :startTime))) AND "+
            "b.bookingStatus = :bookingStatus")
    List<Booking> findExactConflictingBookings(
            @Param("serviceId") Long serviceId,
            @Param("fromDate") LocalDate fromDate,
            @Param("startTime") LocalTime startTime,
            @Param("toDate") LocalDate toDate,
            @Param("endTime") LocalTime endTime,
            @Param("bookingStatus") BookingStatus bookingStatus
    );

    @Query("SELECT b FROM Booking b WHERE " +
            "b.tourist.userId = :userId AND " +
            // Date range overlap condition (more flexible than exact match)
            "((b.fromDate <= :toDate AND b.toDate >= :fromDate) AND " +
            // Time slot overlap condition
            "(b.startTime < :endTime AND b.endTime > :startTime)) AND "+
            "b.bookingStatus = :bookingStatus")
    List<Booking> findOverlappingBookings(
            @Param("userId") Long userId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("bookingStatus") BookingStatus bookingStatus
    );

    @Query("SELECT b FROM Booking b WHERE :date BETWEEN b.fromDate AND b.toDate AND b.service.serviceId = :serviceId AND b.bookingStatus = :bookingStatus")
    List<Booking> findBookingsOnADay (
            @Param("date") LocalDate date,
            @Param("serviceId") Long serviceId,
            @Param("bookingStatus") BookingStatus bookingStatus
    );

    List<Booking> findByService_ServiceIdAndTourist_UserId(Long serviceId, Long touristId);

}
