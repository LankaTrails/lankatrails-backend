package com.lankatrails.lankatrails_backend.service.impl;

import com.lankatrails.lankatrails_backend.dtos.AvailabilityDto;
import com.lankatrails.lankatrails_backend.dtos.BookingItemDto;
import com.lankatrails.lankatrails_backend.dtos.ProviderDto;
import com.lankatrails.lankatrails_backend.dtos.request.BookingRequestDTO;
import com.lankatrails.lankatrails_backend.dtos.request.LocationDTO;
import com.lankatrails.lankatrails_backend.dtos.request.PaymentRequestDto;
import com.lankatrails.lankatrails_backend.dtos.request.ServiceDTO;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.AvailabilityResponse;
import com.lankatrails.lankatrails_backend.dtos.response.BookingResponseDTO;
import com.lankatrails.lankatrails_backend.exception.BadRequestException;
import com.lankatrails.lankatrails_backend.exception.ResourceNotFoundException;
import com.lankatrails.lankatrails_backend.model.Booking;
import com.lankatrails.lankatrails_backend.model.Service;
import com.lankatrails.lankatrails_backend.model.TripItem;
import com.lankatrails.lankatrails_backend.model.TripParticipant;
import com.lankatrails.lankatrails_backend.model.enums.BookingStatus;
import com.lankatrails.lankatrails_backend.model.enums.TripItemType;
import com.lankatrails.lankatrails_backend.model.enums.TripPrivilege;
import com.lankatrails.lankatrails_backend.repositories.BookingRepository;
import com.lankatrails.lankatrails_backend.repositories.ServiceRepository;
import com.lankatrails.lankatrails_backend.repositories.TripItemRepository;
import com.lankatrails.lankatrails_backend.repositories.TripParticipantRepository;
import com.lankatrails.lankatrails_backend.security.utils.AuthUtils;
import com.lankatrails.lankatrails_backend.service.AvailabilityService;
import com.lankatrails.lankatrails_backend.service.BookingService;
import com.lankatrails.lankatrails_backend.service.PaymentService;
import com.lankatrails.lankatrails_backend.service.utils.TripPrivilegeUtils;
import com.stripe.model.PaymentIntent;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
@Slf4j
public class BookingServiceImpl implements BookingService {

    @Autowired
    BookingRepository bookingRepository;

    @Autowired
    ServiceRepository serviceRepository;

    @Autowired
    TripParticipantRepository tripParticipantRepository;

    @Autowired
    TripItemRepository tripItemRepository;

    @Autowired
    TripPrivilegeUtils tripPrivilegeUtils;

    @Autowired
    AuthUtils authUtils;

    @Autowired
    AvailabilityService availabilityService;

    @Autowired
    PaymentService paymentService;

    @Autowired
    ModelMapper modelMapper;

    @Override
    @Transactional
    public APIResponse<AvailabilityResponse> checkAvailability(AvailabilityDto availabilityDto) {
        // Delegate to the availability service
        return availabilityService.checkAvailability(availabilityDto);
    }

    @Override
    @Transactional
    public APIResponse<PaymentRequestDto> addNewBooking(Long tripItemId) {
        TripParticipant tripParticipant = tripParticipantRepository.findByTourist_UserId(authUtils.loggedInUserId())
                .orElseThrow(() -> new RuntimeException("Trip Participant not found"));

        // Validate participant's privilege to book for the trip
        if (!tripPrivilegeUtils.hasPrivilege(tripParticipant.getTripRole(), TripPrivilege.ADD_BOOKINGS)) {
            throw new BadRequestException("Insufficient privileges to add bookings to this trip");
        }

        TripItem tripItem = tripItemRepository.findById(tripItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip Item", tripItemId));

        // Use pessimistic locking to prevent race conditions during booking
        Service serviceWithLock = serviceRepository.findByIdWithLock(tripItem.getService().getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("ServiceID", tripItem.getService().getServiceId()));

        AvailabilityDto availabilityDto = AvailabilityDto.builder()
                .startDateTime(tripItem.getStartTime())
                .endDateTime(tripItem.getEndTime())
                .adultCount(tripItem.getNumberOfAdults())
                .childCount(tripItem.getNumberOfChildren())
                .serviceId(serviceWithLock.getServiceId())
                .tripId(tripItem.getTrip().getTripId())
                .noOfUnits(tripItem.getNoOfUnits())
                .build();

        // Use the availability service to check availability
        APIResponse<String> availabilityResponse = availabilityService.validateServiceAvailability(availabilityDto);

        if (availabilityResponse.isSuccess()) {
            // Calculate deposit amount safely
            BigDecimal depositAmount = BigDecimal.ZERO;
            if (Boolean.TRUE.equals(serviceWithLock.getPriceConfiguration().getRequiresDeposit())) {
                BigDecimal configDepositAmount = serviceWithLock.getPriceConfiguration().getDepositAmount();
                if (configDepositAmount != null) {
                    depositAmount = configDepositAmount;
                }
            }

            Booking newBooking = Booking.builder()
                    .startDateTime(tripItem.getStartTime())
                    .endDateTime(tripItem.getEndTime())
                    .tripItem(tripItem)
                    .tripParticipant(tripParticipant)
                    .bookedDateTime(LocalDate.now().atTime(LocalTime.now()))
                    .totalPrice(serviceWithLock.getPriceConfiguration().calculateTotalPrice(
                            tripItem.getNumberOfAdults(),
                            tripItem.getNumberOfChildren(),
                            tripItem.getNoOfUnits()
                    ))
                    .depositAmount(depositAmount)
                    .paidAmount(BigDecimal.ZERO)
                    .bookingStatus(BookingStatus.PENDING)
                    .build();

            Booking savedBooking = bookingRepository.save(newBooking);

            try {
                // Create payment intent
                PaymentIntent paymentIntent = paymentService.createPaymentIntent(savedBooking.getBookingId());

                return APIResponse.<PaymentRequestDto>builder()
                        .success(true)
                        .message("Booking created successfully. Proceed to payment.")
                        .data(PaymentRequestDto.builder()
                                .bookingId(savedBooking.getBookingId())
                                .paymentIntentId(paymentIntent.getId())
                                .clientSecret(paymentIntent.getClientSecret())
                                .paymentAmount(BigDecimal.valueOf(paymentIntent.getAmount()).divide(BigDecimal.valueOf(100), java.math.RoundingMode.HALF_UP)) // Convert cents to dollars
                                .currency(paymentIntent.getCurrency())
                                .build())
                        .build();
            } catch (Exception e) {
                newBooking.setBookingStatus(BookingStatus.PAYMENT_FAILED);
                bookingRepository.save(newBooking);

                log.error("Error creating payment intent for booking ID: " + newBooking.getBookingId(), e);
                throw new RuntimeException("Failed to create payment intent: " + e.getMessage());
            }
        }

        return APIResponse.<PaymentRequestDto>builder()
                .success(false)
                .message(availabilityResponse.getMessage())
                .data(null)
                .build();
    }

    //find the bookings a service has on a particular day
    @Override
    @Transactional(readOnly = true)
    public APIResponse<BookingResponseDTO> getBookingsOnTheDay(AvailabilityDto availabilityDto, Long id) {
        List<Booking> bookings = bookingRepository.findBookingsOnADay(availabilityDto.getStartDateTime().toLocalDate(), id, BookingStatus.CONFIRMED);
        List<BookingRequestDTO> prepareResponse = new ArrayList<>();
        for (Booking booking : bookings) {
            //map each to BookingRequestDTO
            BookingRequestDTO setResponse = new BookingRequestDTO();
            setResponse.setStartDateTime(booking.getStartDateTime());
            setResponse.setEndDateTime(booking.getEndDateTime());
            setResponse.setBookingStatus(booking.getBookingStatus());
            setResponse.setAdultCount(booking.getTripParticipant().getTrip().getNumberOfAdults());
            setResponse.setChildCount(booking.getTripParticipant().getTrip().getNumberOfChildren());

            prepareResponse.add(setResponse);

        }
        BookingResponseDTO responseDTO = new BookingResponseDTO();
        responseDTO.setContent(prepareResponse);
        return APIResponse.<BookingResponseDTO>builder()
                .success(true)
                .message("")
                .data(responseDTO)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public APIResponse<List<BookingItemDto>> getAllBookingForTrip(Long tripId) {
        List<TripItem> tripItems = tripItemRepository.findByTripItemTypeAndTrip_TripId(TripItemType.SERVICE, tripId);
        List<BookingItemDto> bookingItemDtos = new ArrayList<>();
        for (TripItem tripItem : tripItems) {
            BookingItemDto dto = new BookingItemDto();
            dto.setTripItemId(tripItem.getTripItemId());
            dto.setStartTime(tripItem.getStartTime());
            dto.setEndTime(tripItem.getEndTime());
            dto.setNoOfUnits(tripItem.getNoOfUnits());
            dto.setNumberOfAdults(tripItem.getNumberOfAdults());
            dto.setNumberOfChildren(tripItem.getNumberOfChildren());
            dto.setService(ServiceDTO.builder()
                    .serviceId(tripItem.getService().getServiceId())
                    .serviceName(tripItem.getService().getServiceName())
                    .Category(tripItem.getService().getCategory().getCategoryName())
                    .mainImageUrl(tripItem.getService().getImages().isEmpty() ? null : tripItem.getService().getImages().getFirst().getImageUrl())
                    .locations(tripItem.getService().getLocations().stream()
                            .map(location -> modelMapper.map(location, LocationDTO.class))
                            .collect(Collectors.toSet()))
                    .prices(tripItem.getService().getPriceConfiguration().getPriceWithType())
                    .provider(modelMapper.map(tripItem.getService().getProvider(), ProviderDto.class))
                    .build());
            if (tripItem.getBooking() != null) {
                dto.setStatus(tripItem.getBooking().getBookingStatus());
                dto.setTotalPrice(tripItem.getBooking().getTotalPrice());
                dto.setDepositAmount(tripItem.getBooking().getDepositAmount());
                dto.setPaidAmount(tripItem.getBooking().getPaidAmount());
                dto.setDueAmount(tripItem.getBooking().getTotalPrice().subtract(tripItem.getBooking().getPaidAmount()));
                dto.setBookingDate(tripItem.getBooking().getBookedDateTime());
            } else {
                dto.setStatus(BookingStatus.PENDING);
                dto.setTotalPrice(tripItem.getService().getPriceConfiguration().calculateTotalPrice(
                        tripItem.getNumberOfAdults(),
                        tripItem.getNumberOfChildren(),
                        tripItem.getNoOfUnits()
                ));
                dto.setDepositAmount(BigDecimal.ZERO);
                dto.setPaidAmount(BigDecimal.ZERO);
                dto.setDueAmount(dto.getTotalPrice());
                dto.setBookingDate(null);

                // check the availability for the trip item
                AvailabilityDto availabilityDto = AvailabilityDto.builder()
                        .startDateTime(tripItem.getStartTime())
                        .endDateTime(tripItem.getEndTime())
                        .adultCount(tripItem.getNumberOfAdults())
                        .childCount(tripItem.getNumberOfChildren())
                        .serviceId(tripItem.getService().getServiceId())
                        .tripId(tripItem.getTrip().getTripId())
                        .noOfUnits(tripItem.getNoOfUnits())
                        .build();
                APIResponse<String> availabilityResponse = availabilityService.validateServiceAvailability(availabilityDto);
                if (!availabilityResponse.isSuccess()) {
                    dto.setStatus(BookingStatus.NOT_AVAILABLE);
                }
            }
            bookingItemDtos.add(dto);
        }
        return APIResponse.<List<BookingItemDto>>builder()
                .success(true)
                .message("Bookings retrieved successfully")
                .data(bookingItemDtos)
                .build();
    }

    @Override
    @Transactional
    public APIResponse<String> cancelItem(Long tripItemId) {
        log.info("Canceling booking for trip item with ID: {}", tripItemId);

        try {
            // Find the trip item
            TripItem tripItem = tripItemRepository.findById(tripItemId)
                    .orElseThrow(() -> new ResourceNotFoundException("Trip Item", tripItemId));

            // Get the current user
            Long currentUserId = authUtils.loggedInUserId();

            // Find the trip participant for the current user
            TripParticipant tripParticipant = tripParticipantRepository
                    .findByTourist_UserId(currentUserId)
                    .orElseThrow(() -> new BadRequestException("You are not a participant of any trip"));

            // Verify that the trip participant belongs to the same trip as the trip item
            if (!tripParticipant.getTrip().getTripId().equals(tripItem.getTrip().getTripId())) {
                throw new BadRequestException("You are not a participant of this trip");
            }

            // Check if user has privilege to cancel bookings
            if (!tripPrivilegeUtils.hasPrivilege(tripParticipant.getTripRole(), TripPrivilege.CANCEL_BOOKINGS)) {
                throw new BadRequestException("You don't have permission to cancel bookings for this trip");
            }

            // If the trip item has a booking, delete it first
            if (tripItem.getBooking() != null) {
                Booking booking = tripItem.getBooking();

                // Check if booking is already canceled
                if (booking.getBookingStatus() == BookingStatus.CANCELLED) {
                    return APIResponse.<String>builder()
                            .success(false)
                            .message("Booking is already canceled")
                            .data(null)
                            .build();
                }

                // Delete the booking first
                bookingRepository.delete(booking);
                log.info("Deleted booking for trip item ID: {}", tripItemId);
            }

            // Delete the trip item from the trip_items table
            tripItemRepository.delete(tripItem);

            log.info("Successfully removed trip item and its booking with ID: {}", tripItemId);
            return APIResponse.<String>builder()
                    .success(true)
                    .message("Service removed from trip successfully")
                    .data("Trip item removed for ID: " + tripItemId)
                    .build();

        } catch (ResourceNotFoundException | BadRequestException e) {
            log.error("Error canceling booking for trip item ID {}: {}", tripItemId, e.getMessage());
            return APIResponse.<String>builder()
                    .success(false)
                    .message(e.getMessage())
                    .data(null)
                    .build();
        } catch (Exception e) {
            log.error("Unexpected error canceling booking for trip item ID {}: {}", tripItemId, e.getMessage());
            return APIResponse.<String>builder()
                    .success(false)
                    .message("Failed to cancel booking: " + e.getMessage())
                    .data(null)
                    .build();
        }
    }

    @Override
    public APIResponse<List<BookingItemDto>> getBookings(Long serviceId, LocalDateTime from, LocalDateTime to) {
        List<Booking> bookings = bookingRepository.findBookingsInDateRange(serviceId, from, to, BookingStatus.CONFIRMED);
        List<BookingItemDto> bookingItemDtos = bookings.stream().map(booking -> {
            TripItem tripItem = booking.getTripItem();
            BookingItemDto dto = new BookingItemDto();
            dto.setTripItemId(tripItem.getTripItemId());
            dto.setStartTime(tripItem.getStartTime());
            dto.setEndTime(tripItem.getEndTime());
            dto.setNoOfUnits(tripItem.getNoOfUnits());
            dto.setNumberOfAdults(tripItem.getNumberOfAdults());
            dto.setNumberOfChildren(tripItem.getNumberOfChildren());
            dto.setService(ServiceDTO.builder()
                    .serviceId(tripItem.getService().getServiceId())
                    .serviceName(tripItem.getService().getServiceName())
                    .Category(tripItem.getService().getCategory().getCategoryName())
                    .mainImageUrl(tripItem.getService().getImages().isEmpty() ? null : tripItem.getService().getImages().getFirst().getImageUrl())
                    .locations(tripItem.getService().getLocations().stream()
                            .map(location -> modelMapper.map(location, LocationDTO.class))
                            .collect(Collectors.toSet()))
                    .prices(tripItem.getService().getPriceConfiguration().getPriceWithType())
                    .provider(modelMapper.map(tripItem.getService().getProvider(), ProviderDto.class))
                    .build());
            dto.setStatus(booking.getBookingStatus());
            dto.setTotalPrice(booking.getTotalPrice());
            dto.setDepositAmount(booking.getDepositAmount());
            dto.setPaidAmount(booking.getPaidAmount());
            dto.setDueAmount(booking.getTotalPrice().subtract(booking.getPaidAmount()));
            dto.setBookingDate(booking.getBookedDateTime());
            return dto;
        }).collect(Collectors.toList());

        return APIResponse.<List<BookingItemDto>>builder()
                .success(true)
                .message("Bookings retrieved successfully")
                .data(bookingItemDtos)
                .build();
    }

    @Override
    public Long countBookingsForServiceInPeriod(Long serviceId, LocalDateTime from, LocalDateTime to) {
        return bookingRepository.countBookingsInDateRange(serviceId, from, to, BookingStatus.CONFIRMED);
    }

    @Override
    public Long countFutureBookingsForService(Long serviceId, LocalDateTime from) {
        return bookingRepository.countBookingsInFuture(serviceId, from, BookingStatus.CONFIRMED);
    }

    @Override
    public Long countPastBookingsForService(Long serviceId, LocalDateTime to) {
        return bookingRepository.countBookingsInPast(serviceId, to, BookingStatus.CONFIRMED);
    }

    // Admin-specific methods implementation
    @Override
    @Transactional(readOnly = true)
    public APIResponse<List<BookingItemDto>> getAllBookings() {
        try {
            List<Booking> bookings = bookingRepository.findAll();
            List<BookingItemDto> bookingDtos = bookings.stream()
                    .map(this::convertToBookingItemDto)
                    .collect(Collectors.toList());

            return APIResponse.<List<BookingItemDto>>builder()
                    .success(true)
                    .message("All bookings retrieved successfully")
                    .data(bookingDtos)
                    .build();
        } catch (Exception e) {
            log.error("Error retrieving all bookings", e);
            return APIResponse.<List<BookingItemDto>>builder()
                    .success(false)
                    .message("Failed to retrieve bookings")
                    .data(new ArrayList<>())
                    .build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public APIResponse<List<BookingItemDto>> getBookingsByStatus(String status) {
        try {
            BookingStatus bookingStatus = BookingStatus.valueOf(status.toUpperCase());
            List<Booking> bookings = bookingRepository.findByBookingStatus(bookingStatus);
            List<BookingItemDto> bookingDtos = bookings.stream()
                    .map(this::convertToBookingItemDto)
                    .collect(Collectors.toList());

            return APIResponse.<List<BookingItemDto>>builder()
                    .success(true)
                    .message("Bookings with status " + status + " retrieved successfully")
                    .data(bookingDtos)
                    .build();
        } catch (IllegalArgumentException e) {
            return APIResponse.<List<BookingItemDto>>builder()
                    .success(false)
                    .message("Invalid booking status: " + status)
                    .data(new ArrayList<>())
                    .build();
        } catch (Exception e) {
            log.error("Error retrieving bookings by status", e);
            return APIResponse.<List<BookingItemDto>>builder()
                    .success(false)
                    .message("Failed to retrieve bookings by status")
                    .data(new ArrayList<>())
                    .build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public APIResponse<List<BookingItemDto>> getBookingsByDateRange(LocalDateTime from, LocalDateTime to) {
        try {
            List<Booking> bookings = bookingRepository.findByBookedDateTimeBetween(from, to);
            List<BookingItemDto> bookingDtos = bookings.stream()
                    .map(this::convertToBookingItemDto)
                    .collect(Collectors.toList());

            return APIResponse.<List<BookingItemDto>>builder()
                    .success(true)
                    .message("Bookings in date range retrieved successfully")
                    .data(bookingDtos)
                    .build();
        } catch (Exception e) {
            log.error("Error retrieving bookings by date range", e);
            return APIResponse.<List<BookingItemDto>>builder()
                    .success(false)
                    .message("Failed to retrieve bookings by date range")
                    .data(new ArrayList<>())
                    .build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public APIResponse<Map<String, Object>> getBookingStatistics() {
        try {
            Map<String, Object> statistics = new java.util.HashMap<>();

            // Total bookings count
            Long totalBookings = bookingRepository.count();
            statistics.put("totalBookings", totalBookings);

            // Bookings by status
            Map<String, Long> bookingsByStatus = new java.util.HashMap<>();
            for (BookingStatus status : BookingStatus.values()) {
                Long count = bookingRepository.countByBookingStatus(status);
                bookingsByStatus.put(status.name(), count);
            }
            statistics.put("bookingsByStatus", bookingsByStatus);

            // Total revenue
            BigDecimal totalRevenue = bookingRepository.sumTotalPaidAmount();
            statistics.put("totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO);

            // Recent bookings count (last 30 days)
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            Long recentBookingsCount = bookingRepository.countByBookedDateTimeAfter(thirtyDaysAgo);
            statistics.put("recentBookingsCount", recentBookingsCount);

            return APIResponse.<Map<String, Object>>builder()
                    .success(true)
                    .message("Booking statistics retrieved successfully")
                    .data(statistics)
                    .build();
        } catch (Exception e) {
            log.error("Error retrieving booking statistics", e);
            return APIResponse.<Map<String, Object>>builder()
                    .success(false)
                    .message("Failed to retrieve booking statistics")
                    .data(new java.util.HashMap<>())
                    .build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public APIResponse<List<BookingItemDto>> getRecentBookings(Integer limit) {
        try {
            List<Booking> bookings = bookingRepository.findTop10ByOrderByBookedDateTimeDesc();
            if (limit != null && limit > 0) {
                bookings = bookings.stream().limit(limit).collect(Collectors.toList());
            }

            List<BookingItemDto> bookingDtos = bookings.stream()
                    .map(this::convertToBookingItemDto)
                    .collect(Collectors.toList());

            return APIResponse.<List<BookingItemDto>>builder()
                    .success(true)
                    .message("Recent bookings retrieved successfully")
                    .data(bookingDtos)
                    .build();
        } catch (Exception e) {
            log.error("Error retrieving recent bookings", e);
            return APIResponse.<List<BookingItemDto>>builder()
                    .success(false)
                    .message("Failed to retrieve recent bookings")
                    .data(new ArrayList<>())
                    .build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public APIResponse<Map<String, Object>> getTopServicesAnalytics(Integer limit) {
        try {
            Map<String, Object> analytics = new java.util.HashMap<>();

            // Top services by booking count
            List<Object[]> topServices = bookingRepository.getTopServicesByBookingCount();
            List<Map<String, Object>> serviceData = new ArrayList<>();

            int serviceLimit = limit != null ? limit : 10;
            for (Object[] row : topServices.stream().limit(serviceLimit).collect(Collectors.toList())) {
                Map<String, Object> service = new java.util.HashMap<>();
                service.put("serviceName", row[0]);
                service.put("bookingCount", row[1]);
                serviceData.add(service);
            }
            analytics.put("topServices", serviceData);

            return APIResponse.<Map<String, Object>>builder()
                    .success(true)
                    .message("Top services analytics retrieved successfully")
                    .data(analytics)
                    .build();
        } catch (Exception e) {
            log.error("Error retrieving top services analytics", e);
            return APIResponse.<Map<String, Object>>builder()
                    .success(false)
                    .message("Failed to retrieve top services analytics")
                    .data(new java.util.HashMap<>())
                    .build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public APIResponse<Map<String, Object>> getDashboardAnalytics() {
        try {
            Map<String, Object> dashboard = new java.util.HashMap<>();

            // Key Performance Indicators (KPIs)
            Map<String, Object> kpis = new java.util.HashMap<>();

            // Total bookings count
            Long totalBookings = bookingRepository.count();
            kpis.put("totalBookings", totalBookings);

            // Total unique tourists
            Long totalUniqueTourists = bookingRepository.countUniqueTourists(BookingStatus.CONFIRMED);
            kpis.put("totalUniqueTourists", totalUniqueTourists);

            // Total revenue
            BigDecimal totalRevenue = bookingRepository.sumTotalPaidAmount();
            kpis.put("totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO);

            // Average booking value
            BigDecimal avgBookingValue = bookingRepository.getAverageBookingValue();
            kpis.put("averageBookingValue", avgBookingValue);

            dashboard.put("kpis", kpis);

            // Recent Performance (Last 30 days)
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            LocalDateTime now = LocalDateTime.now();

            Map<String, Object> recentPerformance = new java.util.HashMap<>();

            // Recent bookings count
            Long recentBookingsCount = bookingRepository.countByBookedDateTimeAfter(thirtyDaysAgo);
            recentPerformance.put("recentBookingsCount", recentBookingsCount);

            // Recent tourists
            Long recentTourists = bookingRepository.countUniqueTouristsByDateRange(thirtyDaysAgo, now);
            recentPerformance.put("recentTourists", recentTourists);

            // Recent revenue
            BigDecimal recentRevenue = bookingRepository.sumRevenueByDateRange(thirtyDaysAgo, now);
            recentPerformance.put("recentRevenue", recentRevenue != null ? recentRevenue : BigDecimal.ZERO);

            dashboard.put("recentPerformance", recentPerformance);

            // Booking Status Distribution
            Map<String, Long> bookingsByStatus = new java.util.HashMap<>();
            for (BookingStatus status : BookingStatus.values()) {
                Long count = bookingRepository.countByBookingStatus(status);
                bookingsByStatus.put(status.name(), count);
            }
            dashboard.put("bookingStatusDistribution", bookingsByStatus);

            // Top 5 Services
            List<Object[]> topServices = bookingRepository.getTopServicesByBookingCount();
            List<Map<String, Object>> topServicesData = new ArrayList<>();
            for (Object[] row : topServices.stream().limit(5).collect(Collectors.toList())) {
                Map<String, Object> service = new java.util.HashMap<>();
                service.put("serviceName", row[0]);
                service.put("bookingCount", row[1]);
                topServicesData.add(service);
            }
            dashboard.put("topServices", topServicesData);

            // Top 5 Providers
            List<Object[]> topProviders = bookingRepository.getTopProvidersByBookingCount();
            List<Map<String, Object>> topProvidersData = new ArrayList<>();
            for (Object[] row : topProviders.stream().limit(5).collect(Collectors.toList())) {
                Map<String, Object> provider = new java.util.HashMap<>();
                provider.put("providerName", row[0]);
                provider.put("bookingCount", row[1]);
                topProvidersData.add(provider);
            }
            dashboard.put("topProviders", topProvidersData);

            // Monthly trend for current year
            int currentYear = LocalDateTime.now().getYear();
            List<Object[]> monthlyStats = bookingRepository.getMonthlyBookingStatistics(currentYear);
            List<Map<String, Object>> monthlyTrend = new ArrayList<>();
            for (Object[] row : monthlyStats) {
                Map<String, Object> monthData = new java.util.HashMap<>();
                monthData.put("month", row[0]);
                monthData.put("bookingCount", row[1]);
                monthData.put("revenue", row[2]);
                monthlyTrend.add(monthData);
            }
            dashboard.put("monthlyTrend", monthlyTrend);
            dashboard.put("currentYear", currentYear);

            // Growth Metrics (comparing last 30 days with previous 30 days)
            LocalDateTime sixtyDaysAgo = LocalDateTime.now().minusDays(60);
            Long previousPeriodBookings = bookingRepository.countUniqueTouristsByDateRange(sixtyDaysAgo, thirtyDaysAgo);
            Long currentPeriodBookings = bookingRepository.countUniqueTouristsByDateRange(thirtyDaysAgo, now);

            Map<String, Object> growthMetrics = new java.util.HashMap<>();
            if (previousPeriodBookings > 0) {
                double growthRate = ((double) (currentPeriodBookings - previousPeriodBookings) / previousPeriodBookings) * 100;
                growthMetrics.put("touristGrowthRate", Math.round(growthRate * 100.0) / 100.0);
            } else {
                growthMetrics.put("touristGrowthRate", 0.0);
            }
            dashboard.put("growthMetrics", growthMetrics);

            return APIResponse.<Map<String, Object>>builder()
                    .success(true)
                    .message("Dashboard analytics retrieved successfully")
                    .data(dashboard)
                    .build();

        } catch (Exception e) {
            log.error("Error retrieving dashboard analytics", e);
            return APIResponse.<Map<String, Object>>builder()
                    .success(false)
                    .message("Failed to retrieve dashboard analytics")
                    .data(new java.util.HashMap<>())
                    .build();
        }
    }

    private BookingItemDto convertToBookingItemDto(Booking booking) {
        TripItem tripItem = booking.getTripItem();
        Service service = tripItem.getService();

        return BookingItemDto.builder()
                .tripItemId(tripItem.getTripItemId())
                .service(ServiceDTO.builder()
                        .serviceId(service.getServiceId())
                        .serviceName(service.getServiceName())
                        .Category(service.getCategory().getCategoryName())
                        .mainImageUrl(service.getImages().isEmpty() ? null : service.getImages().getFirst().getImageUrl())
                        .locations(service.getLocations().stream()
                                .map(location -> modelMapper.map(location, LocationDTO.class))
                                .collect(Collectors.toSet()))
                        .prices(service.getPriceConfiguration().getPriceWithType())
                        .provider(modelMapper.map(service.getProvider(), ProviderDto.class))
                        .build())
                .startTime(tripItem.getStartTime())
                .endTime(tripItem.getEndTime())
                .noOfUnits(tripItem.getNoOfUnits())
                .numberOfAdults(tripItem.getNumberOfAdults())
                .numberOfChildren(tripItem.getNumberOfChildren())
                .status(booking.getBookingStatus())
                .totalPrice(booking.getTotalPrice())
                .paidAmount(booking.getPaidAmount())
                .dueAmount(booking.getTotalPrice().subtract(booking.getPaidAmount()))
                .depositAmount(booking.getDepositAmount())
                .bookingDate(booking.getBookedDateTime())
                .build();
    }

    // Advanced Analytics Methods Implementation
    @Override
    @Transactional(readOnly = true)
    public APIResponse<Map<String, Object>> getTouristAnalytics() {
        try {
            Map<String, Object> analytics = new java.util.HashMap<>();

            // Total unique tourists who made bookings
            Long totalUniqueTourists = bookingRepository.countUniqueTourists(BookingStatus.CONFIRMED);
            analytics.put("totalUniqueTourists", totalUniqueTourists);

            // New tourists this month
            LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusSeconds(1);
            Long newTouristsThisMonth = bookingRepository.countUniqueTouristsByDateRange(startOfMonth, endOfMonth);
            analytics.put("newTouristsThisMonth", newTouristsThisMonth);

            // New tourists last 30 days
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            Long newTouristsLast30Days = bookingRepository.countUniqueTouristsByDateRange(thirtyDaysAgo, LocalDateTime.now());
            analytics.put("newTouristsLast30Days", newTouristsLast30Days);

            return APIResponse.<Map<String, Object>>builder()
                    .success(true)
                    .message("Tourist analytics retrieved successfully")
                    .data(analytics)
                    .build();
        } catch (Exception e) {
            log.error("Error retrieving tourist analytics", e);
            return APIResponse.<Map<String, Object>>builder()
                    .success(false)
                    .message("Failed to retrieve tourist analytics")
                    .data(new java.util.HashMap<>())
                    .build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public APIResponse<Map<String, Object>> getBookingAnalytics(LocalDateTime from, LocalDateTime to) {
        try {
            Map<String, Object> analytics = new java.util.HashMap<>();

            // Total bookings in period
            List<Booking> bookingsInPeriod = bookingRepository.findByBookedDateTimeBetween(from, to);
            analytics.put("totalBookings", bookingsInPeriod.size());

            // Booking status distribution
            List<Object[]> statusDistribution = bookingRepository.getBookingStatusDistribution(from, to);
            Map<String, Long> statusMap = new java.util.HashMap<>();
            for (Object[] row : statusDistribution) {
                statusMap.put(row[0].toString(), (Long) row[1]);
            }
            analytics.put("bookingStatusDistribution", statusMap);

            // Daily booking trends
            List<Object[]> dailyTrends = bookingRepository.getDailyBookingTrends(from, to);
            List<Map<String, Object>> trendData = new ArrayList<>();
            for (Object[] row : dailyTrends) {
                Map<String, Object> dayData = new java.util.HashMap<>();
                dayData.put("date", row[0].toString());
                dayData.put("bookings", row[1]);
                trendData.add(dayData);
            }
            analytics.put("dailyBookingTrends", trendData);

            // Average booking value
            BigDecimal avgBookingValue = bookingRepository.getAverageBookingValue();
            analytics.put("averageBookingValue", avgBookingValue);

            return APIResponse.<Map<String, Object>>builder()
                    .success(true)
                    .message("Booking analytics retrieved successfully")
                    .data(analytics)
                    .build();
        } catch (Exception e) {
            log.error("Error retrieving booking analytics", e);
            return APIResponse.<Map<String, Object>>builder()
                    .success(false)
                    .message("Failed to retrieve booking analytics")
                    .data(new java.util.HashMap<>())
                    .build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public APIResponse<Map<String, Object>> getRevenueAnalytics(LocalDateTime from, LocalDateTime to) {
        try {
            Map<String, Object> analytics = new java.util.HashMap<>();

            // Total revenue in period
            BigDecimal totalRevenue = bookingRepository.sumRevenueByDateRange(from, to);
            analytics.put("totalRevenue", totalRevenue);

            // Total bookings revenue (all time)
            BigDecimal allTimeRevenue = bookingRepository.sumTotalPaidAmount();
            analytics.put("allTimeRevenue", allTimeRevenue);

            // Average booking value
            BigDecimal avgBookingValue = bookingRepository.getAverageBookingValue();
            analytics.put("averageBookingValue", avgBookingValue);

            // Revenue by confirmed bookings in period
            List<Booking> confirmedBookings = bookingRepository.findByBookedDateTimeBetween(from, to)
                    .stream()
                    .filter(b -> b.getBookingStatus() == BookingStatus.CONFIRMED)
                    .collect(Collectors.toList());

            BigDecimal confirmedRevenue = confirmedBookings.stream()
                    .map(Booking::getPaidAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            analytics.put("confirmedRevenue", confirmedRevenue);

            // Pending revenue (unpaid amounts)
            BigDecimal pendingRevenue = confirmedBookings.stream()
                    .map(b -> b.getTotalPrice().subtract(b.getPaidAmount()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            analytics.put("pendingRevenue", pendingRevenue);

            return APIResponse.<Map<String, Object>>builder()
                    .success(true)
                    .message("Revenue analytics retrieved successfully")
                    .data(analytics)
                    .build();
        } catch (Exception e) {
            log.error("Error retrieving revenue analytics", e);
            return APIResponse.<Map<String, Object>>builder()
                    .success(false)
                    .message("Failed to retrieve revenue analytics")
                    .data(new java.util.HashMap<>())
                    .build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public APIResponse<Map<String, Object>> getServiceProviderAnalytics() {
        try {
            Map<String, Object> analytics = new java.util.HashMap<>();

            // Top providers by booking count
            List<Object[]> topProviders = bookingRepository.getTopProvidersByBookingCount();
            List<Map<String, Object>> providerData = new ArrayList<>();
            for (Object[] row : topProviders) {
                Map<String, Object> provider = new java.util.HashMap<>();
                provider.put("providerName", row[0]);
                provider.put("bookingCount", row[1]);
                providerData.add(provider);
            }
            analytics.put("topProviders", providerData.stream().limit(10).collect(Collectors.toList()));

            return APIResponse.<Map<String, Object>>builder()
                    .success(true)
                    .message("Service provider analytics retrieved successfully")
                    .data(analytics)
                    .build();
        } catch (Exception e) {
            log.error("Error retrieving service provider analytics", e);
            return APIResponse.<Map<String, Object>>builder()
                    .success(false)
                    .message("Failed to retrieve service provider analytics")
                    .data(new java.util.HashMap<>())
                    .build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public APIResponse<Map<String, Object>> getMonthlyAnalytics(int year) {
        try {
            Map<String, Object> analytics = new java.util.HashMap<>();

            // Monthly booking statistics
            List<Object[]> monthlyStats = bookingRepository.getMonthlyBookingStatistics(year);
            List<Map<String, Object>> monthlyData = new ArrayList<>();

            for (Object[] row : monthlyStats) {
                Map<String, Object> monthData = new java.util.HashMap<>();
                monthData.put("month", row[0]);
                monthData.put("bookingCount", row[1]);
                monthData.put("revenue", row[2]);
                monthlyData.add(monthData);
            }
            analytics.put("monthlyStatistics", monthlyData);
            analytics.put("year", year);

            return APIResponse.<Map<String, Object>>builder()
                    .success(true)
                    .message("Monthly analytics retrieved successfully")
                    .data(analytics)
                    .build();
        } catch (Exception e) {
            log.error("Error retrieving monthly analytics", e);
            return APIResponse.<Map<String, Object>>builder()
                    .success(false)
                    .message("Failed to retrieve monthly analytics")
                    .data(new java.util.HashMap<>())
                    .build();
        }
    }

}
