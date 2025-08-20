package com.lankatrails.lankatrails_backend.repositories;

import com.lankatrails.lankatrails_backend.model.Tourist;
import com.lankatrails.lankatrails_backend.model.Trip;
import com.lankatrails.lankatrails_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TripRepository extends JpaRepository<Trip, Long> {
//    List<Trip> findByTouristsContaining(Tourist tourist);

    List<Trip> findByParticipants_Tourist(Tourist tourist);

    Optional<Trip> findByTripId(Long tripId);

    @Query("SELECT t FROM Trip t " +
            "JOIN t.participants p " +
            "WHERE p.tourist = :tourist " +
            "AND t.startDate <= :endDate " +
            "AND t.endDate >= :startDate")
    List<Trip> findOverlappingTripsForTourist(
            @Param("tourist") Tourist tourist,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

}
