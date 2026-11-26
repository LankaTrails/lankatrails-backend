package com.lankatrails.lankatrails_backend.service;

import com.lankatrails.lankatrails_backend.dtos.response.ProviderInsightsDTO;
import com.lankatrails.lankatrails_backend.model.Booking;
import com.lankatrails.lankatrails_backend.model.Category;
import com.lankatrails.lankatrails_backend.model.RateAndReview;
import com.lankatrails.lankatrails_backend.model.Service;
import com.lankatrails.lankatrails_backend.model.Tourist;
import com.lankatrails.lankatrails_backend.model.TripItem;
import com.lankatrails.lankatrails_backend.model.enums.BookingStatus;
import com.lankatrails.lankatrails_backend.model.enums.ServiceCategory;
import com.lankatrails.lankatrails_backend.model.enums.ServiceStatus;
import com.lankatrails.lankatrails_backend.repositories.BookingRepository;
import com.lankatrails.lankatrails_backend.repositories.ReviewRepository;
import com.lankatrails.lankatrails_backend.repositories.ServiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProviderInsightsServiceTest {

    private static final long PROVIDER_ID = 10L;
    // 2026-06-15 12:00 UTC: the six-month window is 2026-01 .. 2026-06
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-06-15T12:00:00Z"), ZoneOffset.UTC);
    private static final LocalDateTime NOW = LocalDateTime.now(CLOCK);

    private final ServiceRepository serviceRepository = mock(ServiceRepository.class);
    private final BookingRepository bookingRepository = mock(BookingRepository.class);
    private final ReviewRepository reviewRepository = mock(ReviewRepository.class);
    private final ProviderInsightsService insights =
            new ProviderInsightsService(serviceRepository, bookingRepository, reviewRepository, CLOCK);

    private Service lodge;
    private Service van;

    @BeforeEach
    void setUp() {
        lodge = service(1L, "Riverside Eco Lodge", ServiceCategory.ACCOMMODATION, ServiceStatus.ACTIVE);
        van = service(2L, "Airport Van", ServiceCategory.TRANSPORT, ServiceStatus.INACTIVE);
        when(serviceRepository.findByProvider_UserId(PROVIDER_ID)).thenReturn(List.of(lodge, van));
        when(bookingRepository.findAllForProvider(PROVIDER_ID)).thenReturn(List.of(
                booking(100L, lodge, BookingStatus.CONFIRMED, "30000", NOW.minusMonths(1)),
                booking(101L, lodge, BookingStatus.CONFIRMED, "20000", NOW.minusDays(2)),
                booking(102L, van, BookingStatus.CONFIRMED, "5000", NOW.minusMonths(2)),
                booking(103L, lodge, BookingStatus.PENDING, null, NOW.plusDays(10)),
                booking(104L, van, BookingStatus.CANCELLED, "8000", NOW.minusDays(5)),
                // outside the six-month window, still counted in the total
                booking(105L, lodge, BookingStatus.CONFIRMED, "1000", NOW.minusMonths(8))));
        when(reviewRepository.findByService_Provider_UserId(PROVIDER_ID)).thenReturn(List.of(
                review(1L, lodge, 5L, NOW.minusDays(1)),
                review(2L, lodge, 4L, NOW.minusDays(3)),
                review(3L, van, 4L, NOW.minusDays(9))));
    }

    @Test
    void summarisesAllCategories() {
        ProviderInsightsDTO dto = insights.getInsights(PROVIDER_ID, null);

        assertThat(dto.totalServices()).isEqualTo(2);
        assertThat(dto.activeServices()).isEqualTo(1);
        assertThat(dto.totalBookings()).isEqualTo(6);
        assertThat(dto.upcomingBookings()).isEqualTo(1);
        // only paid, confirmed bookings count: the cancelled one is excluded even though it was paid
        assertThat(dto.totalRevenue()).isEqualByComparingTo("56000");
        assertThat(dto.averageRating()).isEqualTo(4.3);
        assertThat(dto.reviewCount()).isEqualTo(3);
    }

    @Test
    void buildsSixMonthTrendEndingThisMonth() {
        List<ProviderInsightsDTO.MonthlyPoint> monthly = insights.getInsights(PROVIDER_ID, null).monthly();

        assertThat(monthly).extracting(ProviderInsightsDTO.MonthlyPoint::month)
                .containsExactly("2026-01", "2026-02", "2026-03", "2026-04", "2026-05", "2026-06");
        assertThat(monthly.get(5).revenue()).isEqualByComparingTo("20000");
        assertThat(monthly.get(4).revenue()).isEqualByComparingTo("30000");
        assertThat(monthly.get(3).revenue()).isEqualByComparingTo("5000");
        assertThat(monthly.get(0).bookings()).isZero();
    }

    @Test
    void ranksTopServicesByRevenueAndListsRecentActivity() {
        ProviderInsightsDTO dto = insights.getInsights(PROVIDER_ID, null);

        assertThat(dto.topServices()).extracting(ProviderInsightsDTO.TopService::serviceName)
                .containsExactly("Riverside Eco Lodge", "Airport Van");
        assertThat(dto.topServices().get(0).revenue()).isEqualByComparingTo("51000");
        assertThat(dto.recentBookings()).hasSize(5);
        // newest booking first; bookings without a participant fall back to "Guest"
        assertThat(dto.recentBookings().get(0).bookingId()).isEqualTo(103L);
        assertThat(dto.recentBookings().get(0).touristName()).isEqualTo("Guest");
        assertThat(dto.recentReviews().get(0).touristName()).isEqualTo("Amaya Silva");
        assertThat(dto.recentReviews()).extracting(ProviderInsightsDTO.RecentReview::reviewId)
                .containsExactly(1L, 2L, 3L);
    }

    @Test
    void filtersByCategory() {
        ProviderInsightsDTO dto = insights.getInsights(PROVIDER_ID, ServiceCategory.TRANSPORT);

        assertThat(dto.totalServices()).isEqualTo(1);
        assertThat(dto.totalBookings()).isEqualTo(2);
        assertThat(dto.totalRevenue()).isEqualByComparingTo("5000");
        assertThat(dto.averageRating()).isEqualTo(4.0);
        assertThat(dto.topServices()).extracting(ProviderInsightsDTO.TopService::category)
                .containsOnly(ServiceCategory.TRANSPORT);
    }

    @Test
    void handlesProviderWithNoActivity() {
        when(serviceRepository.findByProvider_UserId(PROVIDER_ID)).thenReturn(List.of());
        when(bookingRepository.findAllForProvider(PROVIDER_ID)).thenReturn(List.of());
        when(reviewRepository.findByService_Provider_UserId(PROVIDER_ID)).thenReturn(List.of());

        ProviderInsightsDTO dto = insights.getInsights(PROVIDER_ID, null);

        assertThat(dto.totalRevenue()).isEqualByComparingTo("0");
        assertThat(dto.averageRating()).isNull();
        assertThat(dto.monthly()).hasSize(6);
        assertThat(dto.topServices()).isEmpty();
    }

    private static Service service(long id, String name, ServiceCategory category, ServiceStatus status) {
        Service service = new Service();
        service.setServiceId(id);
        service.setServiceName(name);
        service.setCategory(new Category(category));
        service.setStatus(status);
        return service;
    }

    private static Booking booking(long id, Service service, BookingStatus status, String paid, LocalDateTime start) {
        TripItem item = new TripItem();
        item.setService(service);
        Booking booking = new Booking();
        booking.setBookingId(id);
        booking.setTripItem(item);
        booking.setBookingStatus(status);
        booking.setPaidAmount(paid == null ? null : new BigDecimal(paid));
        booking.setTotalPrice(paid == null ? new BigDecimal("12000") : new BigDecimal(paid));
        booking.setStartDateTime(start);
        booking.setBookedDateTime(start.minusDays(7));
        return booking;
    }

    private static RateAndReview review(long id, Service service, long rate, LocalDateTime created) {
        Tourist tourist = new Tourist();
        tourist.setFirstName("Amaya");
        tourist.setLastName("Silva");
        RateAndReview review = new RateAndReview();
        review.setReviewId(id);
        review.setService(service);
        review.setRate(rate);
        review.setTourist(tourist);
        review.setCreatedDate(created);
        return review;
    }
}
