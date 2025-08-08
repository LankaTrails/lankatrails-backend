package com.lankatrails.lankatrails_backend.repositories;

import com.lankatrails.lankatrails_backend.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking,Long> {
    List<Booking> findByEndTimeAndStartTimeAndFromDateAndToDateAndService_ServiceId(
            LocalTime startTime,
            LocalTime endTime,
            LocalDate fromDate,
            LocalDate endDate,
            Long serviceId
    );

    @Query("SELECT b FROM Booking b WHERE " +
            "b.service.id = :serviceId AND " +
            "((b.fromDate < :toDate OR (b.fromDate = :toDate AND b.startTime <= :endTime)) AND " +
            "(b.toDate > :fromDate OR (b.toDate = :fromDate AND b.endTime >= :startTime)))")
    List<Booking> findExactConflictingBookings(
            @Param("serviceId") Long serviceId,
            @Param("fromDate") LocalDate fromDate,
            @Param("startTime") LocalTime startTime,
            @Param("toDate") LocalDate toDate,
            @Param("endTime") LocalTime endTime
    );
}
