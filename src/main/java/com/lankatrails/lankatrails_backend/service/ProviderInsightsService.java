package com.lankatrails.lankatrails_backend.service;

import com.lankatrails.lankatrails_backend.dtos.response.ProviderInsightsDTO;
import com.lankatrails.lankatrails_backend.dtos.response.ProviderInsightsDTO.MonthlyPoint;
import com.lankatrails.lankatrails_backend.dtos.response.ProviderInsightsDTO.RecentBooking;
import com.lankatrails.lankatrails_backend.dtos.response.ProviderInsightsDTO.RecentReview;
import com.lankatrails.lankatrails_backend.dtos.response.ProviderInsightsDTO.TopService;
import com.lankatrails.lankatrails_backend.model.Booking;
import com.lankatrails.lankatrails_backend.model.RateAndReview;
import com.lankatrails.lankatrails_backend.model.Service;
import com.lankatrails.lankatrails_backend.model.Tourist;
import com.lankatrails.lankatrails_backend.model.enums.BookingStatus;
import com.lankatrails.lankatrails_backend.model.enums.ServiceCategory;
import com.lankatrails.lankatrails_backend.model.enums.ServiceStatus;
import com.lankatrails.lankatrails_backend.repositories.BookingRepository;
import com.lankatrails.lankatrails_backend.repositories.ReviewRepository;
import com.lankatrails.lankatrails_backend.repositories.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Aggregates a provider's bookings and reviews for the provider dashboard.
 * Volumes per provider are small, so the aggregation is done in memory.
 */
@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class ProviderInsightsService {

    private static final int MONTHS = 6;
    private static final int RECENT = 5;

    private final ServiceRepository serviceRepository;
    private final BookingRepository bookingRepository;
    private final ReviewRepository reviewRepository;
    private final Clock clock;

    @Transactional(readOnly = true)
    public ProviderInsightsDTO getInsights(Long providerId, ServiceCategory category) {
        List<Service> services = serviceRepository.findByProvider_UserId(providerId).stream()
                .filter(s -> matches(s, category))
                .toList();
        List<Booking> bookings = bookingRepository.findAllForProvider(providerId).stream()
                .filter(b -> matches(b.getTripItem().getService(), category))
                .toList();
        List<RateAndReview> reviews = reviewRepository.findByService_Provider_UserId(providerId).stream()
                .filter(r -> matches(r.getService(), category))
                .toList();

        LocalDateTime now = LocalDateTime.now(clock);
        List<Booking> revenueBookings = bookings.stream().filter(this::countsTowardsRevenue).toList();

        return new ProviderInsightsDTO(
                services.size(),
                services.stream().filter(s -> s.getStatus() == ServiceStatus.ACTIVE).count(),
                bookings.size(),
                bookings.stream()
                        .filter(b -> b.getStartDateTime() != null && b.getStartDateTime().isAfter(now))
                        .filter(b -> b.getBookingStatus() == BookingStatus.CONFIRMED || b.getBookingStatus() == BookingStatus.PENDING)
                        .count(),
                sum(revenueBookings),
                reviews.isEmpty() ? null : round(reviews.stream().mapToLong(r -> r.getRate() == null ? 0 : r.getRate()).average().orElse(0)),
                reviews.size(),
                monthly(revenueBookings, YearMonth.from(now)),
                topServices(revenueBookings),
                bookings.stream()
                        .sorted(Comparator.comparing(Booking::getBookedDateTime, Comparator.nullsLast(Comparator.reverseOrder())))
                        .limit(RECENT)
                        .map(this::toRecentBooking)
                        .toList(),
                reviews.stream()
                        .sorted(Comparator.comparing(RateAndReview::getCreatedDate, Comparator.nullsLast(Comparator.reverseOrder())))
                        .limit(RECENT)
                        .map(this::toRecentReview)
                        .toList()
        );
    }

    private boolean countsTowardsRevenue(Booking booking) {
        return booking.getBookingStatus() == BookingStatus.CONFIRMED
                && booking.getPaidAmount() != null && booking.getPaidAmount().signum() > 0;
    }

    private List<MonthlyPoint> monthly(List<Booking> bookings, YearMonth current) {
        Map<YearMonth, List<Booking>> byMonth = new LinkedHashMap<>();
        for (int i = MONTHS - 1; i >= 0; i--) {
            byMonth.put(current.minusMonths(i), new ArrayList<>());
        }
        for (Booking booking : bookings) {
            if (booking.getStartDateTime() == null) continue;
            List<Booking> bucket = byMonth.get(YearMonth.from(booking.getStartDateTime()));
            if (bucket != null) bucket.add(booking);
        }
        return byMonth.entrySet().stream()
                .map(e -> new MonthlyPoint(e.getKey().toString(), e.getValue().size(), sum(e.getValue())))
                .toList();
    }

    private List<TopService> topServices(List<Booking> bookings) {
        return bookings.stream()
                .collect(Collectors.groupingBy(b -> b.getTripItem().getService(), LinkedHashMap::new, Collectors.toList()))
                .entrySet().stream()
                .map(e -> new TopService(e.getKey().getServiceId(), e.getKey().getServiceName(), category(e.getKey()),
                        e.getValue().size(), sum(e.getValue())))
                .sorted(Comparator.comparing(TopService::revenue).reversed())
                .limit(RECENT)
                .toList();
    }

    private RecentBooking toRecentBooking(Booking b) {
        Service service = b.getTripItem().getService();
        Tourist tourist = b.getTripParticipant() != null ? b.getTripParticipant().getTourist() : null;
        return new RecentBooking(b.getBookingId(), service.getServiceId(), service.getServiceName(), category(service),
                name(tourist), b.getStartDateTime(), b.getBookingStatus(), b.getTotalPrice());
    }

    private RecentReview toRecentReview(RateAndReview r) {
        return new RecentReview(r.getReviewId(), r.getService().getServiceId(), r.getService().getServiceName(),
                name(r.getTourist()), r.getRate(), r.getReview(), r.getCreatedDate());
    }

    private static boolean matches(Service service, ServiceCategory category) {
        return category == null || Objects.equals(category(service), category);
    }

    private static ServiceCategory category(Service service) {
        return service.getCategory() != null ? service.getCategory().getCategoryName() : null;
    }

    private static String name(Tourist tourist) {
        if (tourist == null) return "Guest";
        String full = ((tourist.getFirstName() == null ? "" : tourist.getFirstName()) + " "
                + (tourist.getLastName() == null ? "" : tourist.getLastName())).trim();
        return full.isEmpty() ? "Guest" : full;
    }

    private static BigDecimal sum(List<Booking> bookings) {
        return bookings.stream().map(Booking::getPaidAmount).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static Double round(double value) {
        return BigDecimal.valueOf(value).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }
}
