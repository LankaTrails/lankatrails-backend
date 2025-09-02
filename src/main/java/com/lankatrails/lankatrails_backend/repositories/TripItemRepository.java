package com.lankatrails.lankatrails_backend.repositories;

import com.lankatrails.lankatrails_backend.model.Trip;
import com.lankatrails.lankatrails_backend.model.TripItem;
import com.lankatrails.lankatrails_backend.model.enums.TripItemType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TripItemRepository extends JpaRepository<TripItem, Long> {
    List<TripItem> findByTrip_TripId(Long tripId);

    List<TripItem> findByTripItemTypeAndTrip_TripId(TripItemType tripItemType, Long tripId);

    @Query("SELECT ti FROM TripItem ti " +
            "WHERE ti.trip.tripId = :tripId " +
            "AND ti.startTime <= :endTime " +
            "AND ti.endTime >= :startTime " +
            "ORDER BY ti.startTime")
    List<TripItem> findOverlappingTripItemsForTripId(
            @Param("tripId") Long tripId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    // Inclusive boundaries: counts touching intervals as overlap
    @Query("SELECT CASE WHEN COUNT(ti) > 0 THEN TRUE ELSE FALSE END " +
            "FROM TripItem ti " +
            "WHERE ti.trip.tripId = :tripId " +
            "AND ti.startTime <= :endTime " +
            "AND ti.endTime >= :startTime")
    boolean existsOverlappingTripItemsForTripId(
            @Param("tripId") Long tripId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    Optional<TripItem> findTopByTripAndEndTimeLessThanEqualOrderByEndTimeDesc(
            Trip trip,
            LocalDateTime time
    );

    Optional<TripItem> findTopByTripAndStartTimeGreaterThanEqualOrderByStartTimeAsc(Trip trip, LocalDateTime time);

}
