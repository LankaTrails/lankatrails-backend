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

    @Query("SELECT b FROM Booking b WHERE " +
            "b.tripItem.service.serviceId = :serviceId AND " +
            "b.bookingStatus = :status AND " +
            "(b.startDateTime < :end AND b.endDateTime > :start)")
    List<Booking> findBookingsInDateRange(@Param("serviceId") Long serviceId,
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

    @Query("SELECT COUNT(b) FROM Booking b WHERE " +
            "b.tripItem.service.serviceId = :serviceId AND " +
            "b.bookingStatus = :status AND " +
            "(b.startDateTime < :end AND b.endDateTime > :start)")
    Long countBookingsInDateRange(@Param("serviceId") Long serviceId,
                                  @Param("start") LocalDateTime start,
                                  @Param("end") LocalDateTime end,
                                  @Param("status") BookingStatus status);

    @Query("SELECT COUNT(b) FROM Booking b WHERE " +
            "b.tripItem.service.serviceId = :serviceId AND " +
            "b.bookingStatus = :status AND " +
            "b.endDateTime < :start")
    Long countBookingsInPast(@Param("serviceId") Long serviceId,
                             @Param("start") LocalDateTime start,
                             @Param("status") BookingStatus status);

    @Query("SELECT COUNT(b) FROM Booking b WHERE " +
            "b.tripItem.service.serviceId = :serviceId AND " +
            "b.bookingStatus = :status AND " +
            "b.startDateTime > :start")
    Long countBookingsInFuture(@Param("serviceId") Long serviceId,
                               @Param("start") LocalDateTime start,
                               @Param("status") BookingStatus status);

    // Admin-specific repository methods
    List<Booking> findByBookingStatus(BookingStatus status);

    List<Booking> findByBookedDateTimeBetween(LocalDateTime from, LocalDateTime to);

    Long countByBookingStatus(BookingStatus status);

    @Query("SELECT COALESCE(SUM(b.paidAmount), 0) FROM Booking b")
    java.math.BigDecimal sumTotalPaidAmount();

    Long countByBookedDateTimeAfter(LocalDateTime dateTime);

    List<Booking> findTop10ByOrderByBookedDateTimeDesc();

    // Advanced Analytics Repository Methods
    @Query("SELECT COUNT(DISTINCT b.tripParticipant.tourist.userId) FROM Booking b WHERE b.bookingStatus = :status")
    Long countUniqueTourists(@Param("status") BookingStatus status);

    @Query("SELECT COUNT(DISTINCT b.tripParticipant.tourist.userId) FROM Booking b WHERE b.bookedDateTime BETWEEN :from AND :to")
    Long countUniqueTouristsByDateRange(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT COALESCE(SUM(b.paidAmount), 0) FROM Booking b WHERE b.bookedDateTime BETWEEN :from AND :to AND b.bookingStatus = 'CONFIRMED'")
    java.math.BigDecimal sumRevenueByDateRange(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT COALESCE(AVG(b.totalPrice), 0) FROM Booking b WHERE b.bookingStatus = 'CONFIRMED'")
    java.math.BigDecimal getAverageBookingValue();

    @Query("SELECT b.tripItem.service.serviceName, COUNT(b) as bookingCount FROM Booking b WHERE b.bookingStatus = 'CONFIRMED' GROUP BY b.tripItem.service.serviceName ORDER BY bookingCount DESC")
    List<Object[]> getTopServicesByBookingCount();

    @Query("SELECT b.tripItem.service.provider.businessName, COUNT(b) as bookingCount FROM Booking b WHERE b.bookingStatus = 'CONFIRMED' GROUP BY b.tripItem.service.provider.businessName ORDER BY bookingCount DESC")
    List<Object[]> getTopProvidersByBookingCount();

    @Query("SELECT MONTH(b.bookedDateTime) as month, COUNT(b) as bookingCount, COALESCE(SUM(b.paidAmount), 0) as revenue FROM Booking b WHERE YEAR(b.bookedDateTime) = :year AND b.bookingStatus = 'CONFIRMED' GROUP BY MONTH(b.bookedDateTime) ORDER BY month")
    List<Object[]> getMonthlyBookingStatistics(@Param("year") int year);

    @Query("SELECT DATE(b.bookedDateTime) as bookingDate, COUNT(b) as dailyBookings FROM Booking b WHERE b.bookedDateTime BETWEEN :from AND :to GROUP BY DATE(b.bookedDateTime) ORDER BY bookingDate")
    List<Object[]> getDailyBookingTrends(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT b.bookingStatus, COUNT(b) as statusCount FROM Booking b WHERE b.bookedDateTime BETWEEN :from AND :to GROUP BY b.bookingStatus")
    List<Object[]> getBookingStatusDistribution(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
