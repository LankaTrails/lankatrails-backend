package com.lankatrails.lankatrails_backend.service.impl;

import com.lankatrails.lankatrails_backend.dtos.request.ExternalBookingCreateRequest;
import com.lankatrails.lankatrails_backend.dtos.request.ExternalBookingUpdateRequest;
import com.lankatrails.lankatrails_backend.dtos.response.ExternalBookingResponse;
import com.lankatrails.lankatrails_backend.dtos.response.ExternalBookingStatsResponse;
import com.lankatrails.lankatrails_backend.exception.BadRequestException;
import com.lankatrails.lankatrails_backend.exception.ResourceNotFoundException;
import com.lankatrails.lankatrails_backend.model.ExternalBooking;
import com.lankatrails.lankatrails_backend.model.Service;
import com.lankatrails.lankatrails_backend.model.enums.BookingStatus;
import com.lankatrails.lankatrails_backend.repositories.ExternalBookingRepository;
import com.lankatrails.lankatrails_backend.repositories.ServiceRepository;
import com.lankatrails.lankatrails_backend.service.ExternalBookingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
@Slf4j
@Transactional
public class ExternalBookingServiceImpl implements ExternalBookingService {

    @Autowired
    private ExternalBookingRepository externalBookingRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Override
    public ExternalBookingResponse createExternalBooking(ExternalBookingCreateRequest request) {
        log.info("Creating external booking for service ID: {}", request.getServiceId());

        // Validate service exists
        Service service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with ID: ", request.getServiceId()));

        // Validate trip item exists (if provided)
        Service tripItem = null;
        if (request.getTripItemId() != null) {
            tripItem = serviceRepository.findById(request.getTripItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Trip item not found with ID: ", request.getTripItemId()));
        }

        // Validate date range
        if (request.getStartDateTime().isAfter(request.getEndDateTime())) {
            throw new BadRequestException("Start date time cannot be after end date time");
        }

        // Check for booking conflicts if status is CONFIRMED or PENDING
        if (request.getBookingStatus() == BookingStatus.CONFIRMED ||
                request.getBookingStatus() == BookingStatus.PENDING) {
            if (hasBookingConflict(request.getServiceId(), request.getStartDateTime(), request.getEndDateTime())) {
                throw new BadRequestException("Booking conflict exists for the selected time period");
            }
        }

        // Create external booking
        ExternalBooking externalBooking = ExternalBooking.builder()
                .tripItem(tripItem)
                .service(service)
                .bookedDateTime(LocalDateTime.now())
                .startDateTime(request.getStartDateTime())
                .endDateTime(request.getEndDateTime())
                .totalPrice(request.getTotalPrice())
                .paidAmount(request.getPaidAmount() != null ? request.getPaidAmount() : BigDecimal.ZERO)
                .depositAmount(request.getDepositAmount() != null ? request.getDepositAmount() : BigDecimal.ZERO)
                .bookingStatus(request.getBookingStatus())
                .build();

        externalBooking = externalBookingRepository.save(externalBooking);

        log.info("External booking created with ID: {}", externalBooking.getBookingId());
        return mapToResponse(externalBooking);
    }

    @Override
    public ExternalBookingResponse updateExternalBooking(Long bookingId, ExternalBookingUpdateRequest request) {
        log.info("Updating external booking with ID: {}", bookingId);

        ExternalBooking existingBooking = externalBookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("External booking not found with ID: ", bookingId));

        // Update fields if provided
        if (request.getStartDateTime() != null) {
            existingBooking.setStartDateTime(request.getStartDateTime());
        }

        if (request.getEndDateTime() != null) {
            existingBooking.setEndDateTime(request.getEndDateTime());
        }

        // Validate date range if both dates are set
        if (existingBooking.getStartDateTime().isAfter(existingBooking.getEndDateTime())) {
            throw new BadRequestException("Start date time cannot be after end date time");
        }

        if (request.getTotalPrice() != null) {
            existingBooking.setTotalPrice(request.getTotalPrice());
        }

        if (request.getPaidAmount() != null) {
            existingBooking.setPaidAmount(request.getPaidAmount());
        }

        if (request.getDepositAmount() != null) {
            existingBooking.setDepositAmount(request.getDepositAmount());
        }

        if (request.getBookingStatus() != null) {
            // Check for conflicts if changing to CONFIRMED or PENDING
            if ((request.getBookingStatus() == BookingStatus.CONFIRMED ||
                    request.getBookingStatus() == BookingStatus.PENDING) &&
                    existingBooking.getBookingStatus() != BookingStatus.CONFIRMED &&
                    existingBooking.getBookingStatus() != BookingStatus.PENDING) {

                if (hasBookingConflict(existingBooking.getService().getServiceId(),
                        existingBooking.getStartDateTime(),
                        existingBooking.getEndDateTime())) {
                    throw new BadRequestException("Booking conflict exists for the selected time period");
                }
            }
            existingBooking.setBookingStatus(request.getBookingStatus());
        }

        existingBooking = externalBookingRepository.save(existingBooking);

        log.info("External booking updated successfully with ID: {}", bookingId);
        return mapToResponse(existingBooking);
    }

    @Override
    @Transactional(readOnly = true)
    public ExternalBookingResponse getExternalBookingById(Long bookingId) {
        log.info("Fetching external booking with ID: {}", bookingId);

        ExternalBooking booking = externalBookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("External booking not found with ID: ", bookingId));

        return mapToResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExternalBookingResponse> getExternalBookingsByProvider(Long providerId) {
        log.info("Fetching external bookings for provider ID: {}", providerId);

        List<ExternalBooking> bookings = externalBookingRepository.findByProviderId(providerId);
        return bookings.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExternalBookingResponse> getExternalBookingsByService(Long serviceId) {
        log.info("Fetching external bookings for service ID: {}", serviceId);

        List<ExternalBooking> bookings = externalBookingRepository.findByServiceServiceId(serviceId);
        return bookings.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExternalBookingResponse> getExternalBookingsByStatus(BookingStatus status) {
        log.info("Fetching external bookings with status: {}", status);

        List<ExternalBooking> bookings = externalBookingRepository.findByBookingStatus(status);
        return bookings.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ExternalBookingResponse> getExternalBookingsByProviderAndStatus(Long providerId,
                                                                                BookingStatus status,
                                                                                Pageable pageable) {
        log.info("Fetching external bookings for provider ID: {} with status: {}", providerId, status);

        Page<ExternalBooking> bookings = externalBookingRepository.findByProviderIdAndBookingStatus(
                providerId, status, pageable);

        return bookings.map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExternalBookingResponse> getExternalBookingsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Fetching external bookings between {} and {}", startDate, endDate);

        if (startDate.isAfter(endDate)) {
            throw new BadRequestException("Start date cannot be after end date");
        }

        List<ExternalBooking> bookings = externalBookingRepository.findByDateRange(startDate, endDate);
        return bookings.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ExternalBookingResponse cancelExternalBooking(Long bookingId) {
        log.info("Cancelling external booking with ID: {}", bookingId);

        ExternalBooking booking = externalBookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("External booking not found with ID: ", bookingId));

        if (booking.getBookingStatus() == BookingStatus.CANCELLED) {
            throw new BadRequestException("Booking is already cancelled");
        }

        booking.setBookingStatus(BookingStatus.CANCELLED);
        booking = externalBookingRepository.save(booking);

        log.info("External booking cancelled successfully with ID: {}", bookingId);
        return mapToResponse(booking);
    }

    @Override
    public ExternalBookingResponse confirmExternalBooking(Long bookingId) {
        log.info("Confirming external booking with ID: {}", bookingId);

        ExternalBooking booking = externalBookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("External booking not found with ID: ", bookingId));

        if (booking.getBookingStatus() == BookingStatus.CONFIRMED) {
            throw new BadRequestException("Booking is already confirmed");
        }

        if (booking.getBookingStatus() == BookingStatus.CANCELLED) {
            throw new BadRequestException("Cannot confirm a cancelled booking");
        }

        // Check for conflicts before confirming
        if (hasBookingConflict(booking.getService().getServiceId(),
                booking.getStartDateTime(),
                booking.getEndDateTime())) {
            throw new BadRequestException("Booking conflict exists for the selected time period");
        }

        booking.setBookingStatus(BookingStatus.CONFIRMED);
        booking = externalBookingRepository.save(booking);

        log.info("External booking confirmed successfully with ID: {}", bookingId);
        return mapToResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasBookingConflict(Long serviceId, LocalDateTime startDate, LocalDateTime endDate) {
        List<ExternalBooking> overlappingBookings = externalBookingRepository.findOverlappingBookings(
                serviceId, startDate, endDate);

        return !overlappingBookings.isEmpty();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExternalBookingResponse> getRecentBookingsByProvider(Long providerId) {
        log.info("Fetching recent bookings for provider ID: {}", providerId);

        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<ExternalBooking> bookings = externalBookingRepository.findRecentBookingsByProvider(
                providerId, thirtyDaysAgo);

        return bookings.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteExternalBooking(Long bookingId) {
        log.info("Deleting external booking with ID: {}", bookingId);

        ExternalBooking booking = externalBookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("External booking not found with ID: ", bookingId));

        externalBookingRepository.delete(booking);
        log.info("External booking deleted successfully with ID: {}", bookingId);
    }

    @Override
    @Transactional(readOnly = true)
    public ExternalBookingStatsResponse getBookingStatsByProvider(Long providerId) {
        log.info("Fetching booking statistics for provider ID: {}", providerId);

        List<ExternalBooking> allBookings = externalBookingRepository.findByProviderId(providerId);

        long totalBookings = allBookings.size();
        long confirmedBookings = externalBookingRepository.countByProviderIdAndStatus(providerId, BookingStatus.CONFIRMED);
        long pendingBookings = externalBookingRepository.countByProviderIdAndStatus(providerId, BookingStatus.PENDING);
        long cancelledBookings = externalBookingRepository.countByProviderIdAndStatus(providerId, BookingStatus.CANCELLED);

        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<ExternalBooking> recentBookings = externalBookingRepository.findRecentBookingsByProvider(
                providerId, thirtyDaysAgo);

        return ExternalBookingStatsResponse.builder()
                .totalBookings(totalBookings)
                .confirmedBookings(confirmedBookings)
                .pendingBookings(pendingBookings)
                .cancelledBookings(cancelledBookings)
                .recentBookings((long) recentBookings.size())
                .build();
    }

    private ExternalBookingResponse mapToResponse(ExternalBooking booking) {
        BigDecimal remainingAmount = BigDecimal.ZERO;
        if (booking.getTotalPrice() != null && booking.getPaidAmount() != null) {
            remainingAmount = booking.getTotalPrice().subtract(booking.getPaidAmount());
        }

        return ExternalBookingResponse.builder()
                .bookingId(booking.getBookingId())
                .tripItemId(booking.getTripItem() != null ? booking.getTripItem().getServiceId() : null)
                .serviceId(booking.getService().getServiceId())
                .serviceName(booking.getService().getServiceName())
                .bookedDateTime(booking.getBookedDateTime())
                .startDateTime(booking.getStartDateTime())
                .endDateTime(booking.getEndDateTime())
                .totalPrice(booking.getTotalPrice())
                .paidAmount(booking.getPaidAmount())
                .depositAmount(booking.getDepositAmount())
                .remainingAmount(remainingAmount)
                .bookingStatus(booking.getBookingStatus())
                .build();
    }
}