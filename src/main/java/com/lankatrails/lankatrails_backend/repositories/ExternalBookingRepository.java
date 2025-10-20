package com.lankatrails.lankatrails_backend.repositories;

import com.lankatrails.lankatrails_backend.model.ExternalBooking;
import com.lankatrails.lankatrails_backend.model.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ExternalBookingRepository extends JpaRepository<ExternalBooking, Long> {
    
    // Find all bookings for a specific service
    List<ExternalBooking> findByServiceServiceId(Long serviceId);
    
    // Find all bookings for a specific provider
    @Query("SELECT eb FROM ExternalBooking eb WHERE eb.service.provider.userId = :providerId")
    List<ExternalBooking> findByProviderId(@Param("providerId") Long providerId);
    
    // Find bookings by status
    List<ExternalBooking> findByBookingStatus(BookingStatus status);
    
    // Find bookings by service and status
    List<ExternalBooking> findByServiceServiceIdAndBookingStatus(Long serviceId, BookingStatus status);
    
    // Find bookings by provider and status with pagination
    @Query("SELECT eb FROM ExternalBooking eb WHERE eb.service.provider.userId = :providerId AND eb.bookingStatus = :status")
    Page<ExternalBooking> findByProviderIdAndBookingStatus(@Param("providerId") Long providerId, 
                                                           @Param("status") BookingStatus status, 
                                                           Pageable pageable);
    
    // Find bookings within date range
    @Query("SELECT eb FROM ExternalBooking eb WHERE eb.startDateTime >= :startDate AND eb.endDateTime <= :endDate")
    List<ExternalBooking> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                         @Param("endDate") LocalDateTime endDate);
    
    // Find overlapping bookings for a service
    @Query("SELECT eb FROM ExternalBooking eb WHERE eb.service.serviceId = :serviceId " +
           "AND eb.bookingStatus IN ('CONFIRMED', 'PENDING') " +
           "AND ((eb.startDateTime < :endDate AND eb.endDateTime > :startDate))")
    List<ExternalBooking> findOverlappingBookings(@Param("serviceId") Long serviceId,
                                                 @Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate);
    
    // Find bookings for a specific trip item
    List<ExternalBooking> findByTripItemServiceId(Long tripItemId);
    
    // Count bookings by provider and status
    @Query("SELECT COUNT(eb) FROM ExternalBooking eb WHERE eb.service.provider.userId = :providerId AND eb.bookingStatus = :status")
    Long countByProviderIdAndStatus(@Param("providerId") Long providerId, @Param("status") BookingStatus status);
    
    // Find recent bookings for a provider (last 30 days)
    @Query("SELECT eb FROM ExternalBooking eb WHERE eb.service.provider.userId = :providerId " +
           "AND eb.bookedDateTime >= :sinceDate ORDER BY eb.bookedDateTime DESC")
    List<ExternalBooking> findRecentBookingsByProvider(@Param("providerId") Long providerId, 
                                                      @Param("sinceDate") LocalDateTime sinceDate);
}