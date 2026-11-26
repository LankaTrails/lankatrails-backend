package com.lankatrails.lankatrails_backend.dtos.response;

import com.lankatrails.lankatrails_backend.model.enums.BookingStatus;
import com.lankatrails.lankatrails_backend.model.enums.ServiceCategory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Dashboard summary for the logged-in provider (optionally limited to one service category). */
public record ProviderInsightsDTO(
        long totalServices,
        long activeServices,
        long totalBookings,
        long upcomingBookings,
        BigDecimal totalRevenue,
        Double averageRating,
        long reviewCount,
        List<MonthlyPoint> monthly,
        List<TopService> topServices,
        List<RecentBooking> recentBookings,
        List<RecentReview> recentReviews
) {
    /** month is "YYYY-MM"; the last six months are always present, including empty ones. */
    public record MonthlyPoint(String month, long bookings, BigDecimal revenue) {
    }

    public record TopService(Long serviceId, String serviceName, ServiceCategory category, long bookings, BigDecimal revenue) {
    }

    public record RecentBooking(Long bookingId, Long serviceId, String serviceName, ServiceCategory category, String touristName,
                                LocalDateTime startDateTime, BookingStatus status, BigDecimal totalPrice) {
    }

    public record RecentReview(Long reviewId, Long serviceId, String serviceName, String touristName, Long rate,
                               String review, LocalDateTime createdDate) {
    }
}
