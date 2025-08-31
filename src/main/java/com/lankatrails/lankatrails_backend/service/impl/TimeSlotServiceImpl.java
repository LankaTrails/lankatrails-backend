package com.lankatrails.lankatrails_backend.service.impl;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lankatrails.lankatrails_backend.dtos.AvailabilityDto;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.AvailabilityResponse;
import com.lankatrails.lankatrails_backend.exception.BadRequestException;
import com.lankatrails.lankatrails_backend.exception.ResourceNotFoundException;
import com.lankatrails.lankatrails_backend.model.AvailableTime;
import com.lankatrails.lankatrails_backend.model.Booking;
import com.lankatrails.lankatrails_backend.model.BookingConfiguration;
import com.lankatrails.lankatrails_backend.model.TouristGuide;
import com.lankatrails.lankatrails_backend.model.enums.BookingStatus;
import com.lankatrails.lankatrails_backend.model.enums.BookingType;
import com.lankatrails.lankatrails_backend.repositories.AvailableTimeRepository;
import com.lankatrails.lankatrails_backend.repositories.BookingRepository;
import com.lankatrails.lankatrails_backend.repositories.ServiceRepository;
import com.lankatrails.lankatrails_backend.repositories.TouristGuideRepository;
import com.lankatrails.lankatrails_backend.service.AvailabilityService;
import com.lankatrails.lankatrails_backend.service.TimeSlotService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TimeSlotServiceImpl implements TimeSlotService {

    @Autowired
    private AvailableTimeRepository availableTimeRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private TouristGuideRepository touristGuideRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private AvailabilityService availabilityService;

    @Override
    @Transactional(readOnly = true)
    public APIResponse<List<String>> generateTimeSlots(LocalTime openTime, LocalTime closeTime, Integer minutesPerSlot) {
        log.info("Minutes per slot: {}", minutesPerSlot);
        
        // 1. Validate input
        if (minutesPerSlot <= 0) {
            throw new RuntimeException("Minutes per slot duration should be greater than 0");
        }

        // 2. Create duration
        Duration slotDuration = Duration.ofHours(minutesPerSlot / 60).plusMinutes(minutesPerSlot % 60);
        log.info("Slot duration: {}", slotDuration);

        // 3. Initialize variables
        List<String> slots = new ArrayList<>();
        LocalTime current = openTime;
        int safetyCounter = 0;
        final int MAX_SLOTS = 24; // Absolute maximum for 1-hour slots

        // 4. Generate slots
        while (!current.plus(slotDuration).isAfter(closeTime)
                && safetyCounter++ < MAX_SLOTS && current.isBefore(closeTime)) {

            LocalTime end = current.plus(slotDuration);
            log.info("Adding slot: {} - {}", current, end);
            slots.add(String.format("%02d:%02d - %02d:%02d",
                    current.getHour(), current.getMinute(),
                    end.getHour(), end.getMinute()));

            current = end; // CRITICAL: Update current time

            // Safety check
            if (safetyCounter >= MAX_SLOTS) {
                throw new IllegalStateException("Possible infinite loop detected");
            }
        }

        return APIResponse.<List<String>>builder()
                .success(true)
                .message("Time slots created successfully")
                .data(slots)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public APIResponse<List<String>> getServiceTimeSlots(Long serviceId) {
        return getServiceTimeSlotsForDate(serviceId, LocalDate.now());
    }

    @Override
    @Transactional(readOnly = true)
    public APIResponse<List<String>> getServiceTimeSlotsForDate(Long serviceId, LocalDate requestedDate) {
        com.lankatrails.lankatrails_backend.model.Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Service", serviceId));

        BookingConfiguration config = service.getBookingConfiguration();
        if (config == null) {
            throw new BadRequestException("No booking configuration found for service: " + serviceId);
        }

        // Get the day of the week which the requested date belongs to
        List<AvailableTime> availableTimeList = availableTimeRepository.findByService_ServiceId(serviceId);
        if (availableTimeList.isEmpty()) {
            throw new BadRequestException("No Availability Slots Defined");
        }

        DayOfWeek requestedDay = requestedDate.getDayOfWeek();
        Optional<AvailableTime> daySlot = availableTimeList.stream()
                .filter(slot -> slot.getDayOfWeek().equalsIgnoreCase(requestedDay.toString()))
                .findFirst();

        if (daySlot.isEmpty()) {
            throw new BadRequestException("Service not available on " + requestedDay);
        }

        AvailableTime availableTime = daySlot.get();

        // Check if service is closed on this day
        if (Boolean.TRUE.equals(availableTime.getIsClosed())) {
            return APIResponse.<List<String>>builder()
                    .success(true)
                    .message("Service is closed on " + requestedDay)
                    .data(new ArrayList<>())
                    .build();
        }

        // For 24-hour services, use default slot duration or return empty if not configured
        LocalTime serviceOpenTime = availableTime.getOpenTime();
        LocalTime serviceCloseTime = availableTime.getCloseTime();

        if (Boolean.TRUE.equals(availableTime.getIs24Hours())) {
            // For 24-hour services, use midnight to midnight
            serviceOpenTime = LocalTime.MIDNIGHT;
            serviceCloseTime = LocalTime.of(23, 59);
        }

        // Get the slot duration from booking configuration
        Integer slotDuration = config.getSlotDuration();
        if (slotDuration == null || slotDuration <= 0) {
            throw new BadRequestException("Invalid or missing slot duration in booking configuration");
        }

        return generateTimeSlots(serviceOpenTime, serviceCloseTime, slotDuration);
    }

    @Override
    @Transactional(readOnly = true)
    public APIResponse<List<String>> getAllFreeTimeSlots(AvailabilityDto availabilityDto, Long serviceId) {
        // Check the availability first
        APIResponse<AvailabilityResponse> availabilityCheck = availabilityService.checkAvailability(availabilityDto);
        
        // If not available, return empty slots
        if (!availabilityCheck.isSuccess() || !availabilityCheck.getData().isAvailable()) {
            return APIResponse.<List<String>>builder()
                    .success(true)
                    .message(availabilityCheck.getMessage())
                    .data(new ArrayList<>())
                    .build();
        }

        // Find the service and check booking configuration
        com.lankatrails.lankatrails_backend.model.Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Service", serviceId));

        BookingConfiguration config = service.getBookingConfiguration();
        if (config == null) {
            throw new BadRequestException("No booking configuration found for service: " + serviceId);
        }

        // Only process TIME_SLOTS booking type for free slot calculation
        if (config.getBookingType() != BookingType.TIME_SLOTS) {
            return APIResponse.<List<String>>builder()
                    .success(true)
                    .message("Free time slots only available for TIME_SLOTS booking type")
                    .data(new ArrayList<>())
                    .build();
        }

        // Get all time slots for the requested date
        APIResponse<List<String>> allTimeSlots = getServiceTimeSlotsForDate(serviceId, 
                availabilityDto.getStartDateTime().toLocalDate());
        if (!allTimeSlots.isSuccess()) {
            return allTimeSlots;
        }

        List<String> allTimeSlotsList = allTimeSlots.getData();

        // Get bookings for the requested day using direct repository access to avoid circular dependency
        List<Booking> bookings = bookingRepository.findBookingsOnADay(
                availabilityDto.getStartDateTime().toLocalDate(), serviceId, BookingStatus.BOOKED);

        // Create list of booked time slots
        List<String> bookedSlots = new ArrayList<>();
        for (Booking booking : bookings) {
            String slot = String.format("%02d:%02d - %02d:%02d",
                    booking.getStartDateTime().getHour(), 
                    booking.getStartDateTime().getMinute(),
                    booking.getEndDateTime().getHour(), 
                    booking.getEndDateTime().getMinute());
            bookedSlots.add(slot);
        }

        // Remove the booked time slots from available slots
        List<String> availableForBookingSlots = allTimeSlotsList.stream()
                .filter(item -> !bookedSlots.contains(item))
                .collect(Collectors.toList());

        return APIResponse.<List<String>>builder()
                .success(true)
                .message("Available slots for booking")
                .data(availableForBookingSlots)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public APIResponse<List<String>> getTourGuideDaySlots(Long serviceId) {
        // Find the tour guide
        TouristGuide touristGuide = touristGuideRepository.findByServiceId(serviceId).orElseThrow(
                () -> new ResourceNotFoundException("Tour Guide", serviceId)
        );

        BookingConfiguration config = touristGuide.getBookingConfiguration();
        if (config == null) {
            throw new BadRequestException("No booking configuration found for tour guide service: " + serviceId);
        }

        // Get current date and get availability
        LocalDate currentDate = LocalDate.now();
        List<AvailableTime> availableTimeList = availableTimeRepository.findByService_ServiceId(serviceId);
        if (availableTimeList.isEmpty()) {
            throw new BadRequestException("No Availability Slots Defined");
        }

        DayOfWeek requestedDay = currentDate.getDayOfWeek();
        Optional<AvailableTime> daySlot = availableTimeList.stream()
                .filter(slot -> slot.getDayOfWeek().equalsIgnoreCase(requestedDay.toString()))
                .findFirst();

        if (daySlot.isEmpty()) {
            throw new BadRequestException("Tour guide not available on " + requestedDay);
        }

        AvailableTime availableTime = daySlot.get();

        // Check if service is closed
        if (Boolean.TRUE.equals(availableTime.getIsClosed())) {
            return APIResponse.<List<String>>builder()
                    .success(true)
                    .message("Tour guide is not available on " + requestedDay)
                    .data(new ArrayList<>())
                    .build();
        }

        // Get the open and close times
        LocalTime serviceOpenTime = availableTime.getOpenTime();
        LocalTime serviceCloseTime = availableTime.getCloseTime();

        // Handle 24-hour availability
        if (Boolean.TRUE.equals(availableTime.getIs24Hours())) {
            serviceOpenTime = LocalTime.MIDNIGHT;
            serviceCloseTime = LocalTime.of(23, 59);
        }

        // Get the duration from booking configuration
        Integer duration = config.getSlotDuration();
        if (duration == null || duration <= 0) {
            throw new BadRequestException("Invalid or missing slot duration in tour guide booking configuration");
        }

        return generateTimeSlots(serviceOpenTime, serviceCloseTime, duration);
    }
}
