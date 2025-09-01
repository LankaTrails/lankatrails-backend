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
import com.lankatrails.lankatrails_backend.dtos.request.TimeSlotsRequestDTO;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.TimeSlotsResponseDTO;
import com.lankatrails.lankatrails_backend.exception.BadRequestException;
import com.lankatrails.lankatrails_backend.exception.ResourceNotFoundException;
import com.lankatrails.lankatrails_backend.model.AvailableTime;
import com.lankatrails.lankatrails_backend.model.Booking;
import com.lankatrails.lankatrails_backend.model.BookingConfiguration;
import com.lankatrails.lankatrails_backend.model.BreakTime;
import com.lankatrails.lankatrails_backend.model.TouristGuide;
import com.lankatrails.lankatrails_backend.model.enums.BookingStatus;
import com.lankatrails.lankatrails_backend.model.enums.BookingType;
import com.lankatrails.lankatrails_backend.repositories.AvailableTimeRepository;
import com.lankatrails.lankatrails_backend.repositories.BookingRepository;
import com.lankatrails.lankatrails_backend.repositories.ServiceRepository;
import com.lankatrails.lankatrails_backend.repositories.TouristGuideRepository;
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

    @Override
    @Transactional(readOnly = true)
    public APIResponse<List<String>> generateTimeSlots(LocalTime openTime, LocalTime closeTime, Integer minutesPerSlot) {
        return generateTimeSlotsWithBreakTimes(openTime, closeTime, minutesPerSlot, new ArrayList<>(), 0);
    }

    /**
     * Generate time slots considering break times and buffer time
     */
    private APIResponse<List<String>> generateTimeSlotsWithBreakTimes(LocalTime openTime, LocalTime closeTime, Integer minutesPerSlot, List<BreakTime> breakTimes, Integer bufferTimeMinutes) {
        log.info("Minutes per slot: {}", minutesPerSlot);
        
        // 1. Validate input
        if (minutesPerSlot <= 0) {
            throw new RuntimeException("Minutes per slot duration should be greater than 0");
        }

        // 2. Create duration
        Duration slotDuration = Duration.ofHours(minutesPerSlot / 60).plusMinutes(minutesPerSlot % 60);
        Duration bufferTime = Duration.ofMinutes(bufferTimeMinutes != null ? bufferTimeMinutes : 0);
        log.info("Slot duration: {}, Buffer time: {} minutes", slotDuration, bufferTimeMinutes);

        // 3. Initialize variables
        List<String> slots = new ArrayList<>();
        LocalTime current = openTime;
        int safetyCounter = 0;
        final int MAX_SLOTS = 24; // Absolute maximum for 1-hour slots

        // 4. Generate slots
        while (!current.plus(slotDuration).isAfter(closeTime)
                && safetyCounter++ < MAX_SLOTS && current.isBefore(closeTime)) {

            LocalTime end = current.plus(slotDuration);
            
            // Check if this slot overlaps with any break time
            if (!overlapsWithBreakTime(current, end, breakTimes)) {
                log.info("Adding slot: {} - {}", current, end);
                slots.add(String.format("%02d:%02d - %02d:%02d",
                        current.getHour(), current.getMinute(),
                        end.getHour(), end.getMinute()));
                
                // Move to next potential slot start time = current slot end + buffer time
                current = end.plus(bufferTime);
            } else {
                log.debug("Skipping slot {} - {} due to break time overlap", current, end);
                
                // When overlapping with break time, find the earliest break end time that affects this slot
                LocalTime earliestBreakEnd = findEarliestBreakEndTime(current, end, breakTimes);
                if (earliestBreakEnd != null) {
                    // Start next slot right after the break ends
                    current = earliestBreakEnd;
                } else {
                    // If no specific break end found, advance by minimum increment
                    current = current.plusMinutes(30); // Advance by 30 minutes
                }
            }

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

        return generateTimeSlotsWithBreakTimes(serviceOpenTime, serviceCloseTime, slotDuration, availableTime.getBreakTimes(), config.getBufferTime());
    }

    @Override
    @Transactional(readOnly = true)
    public APIResponse<TimeSlotsResponseDTO> getAllFreeTimeSlots(AvailabilityDto availabilityDto, Long serviceId) {
        log.info("Getting free time slots for service ID: {} on date: {}", serviceId, availabilityDto.getStartDateTime().toLocalDate());
        
        // Find the service and check booking configuration
        com.lankatrails.lankatrails_backend.model.Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Service", serviceId));

        BookingConfiguration config = service.getBookingConfiguration();
        if (config == null) {
            throw new BadRequestException("No booking configuration found for service: " + serviceId);
        }

        // Only process TIME_SLOTS booking type for free slot calculation
        if (config.getBookingType() != BookingType.TIME_SLOTS) {
            return APIResponse.<TimeSlotsResponseDTO>builder()
                    .success(true)
                    .message("Free time slots only available for TIME_SLOTS booking type")
                    .data(new TimeSlotsResponseDTO(new ArrayList<>()))
                    .build();
        }

        // Get the requested date
        LocalDate requestedDate = availabilityDto.getStartDateTime().toLocalDate();
        
        // Get availability for the requested day
        List<AvailableTime> availableTimeList = availableTimeRepository.findByService_ServiceId(serviceId);
        if (availableTimeList.isEmpty()) {
            throw new BadRequestException("No availability slots defined for this service");
        }

        DayOfWeek requestedDay = requestedDate.getDayOfWeek();
        Optional<AvailableTime> daySlot = availableTimeList.stream()
                .filter(slot -> slot.getDayOfWeek().equalsIgnoreCase(requestedDay.toString()))
                .findFirst();

        if (daySlot.isEmpty()) {
            return APIResponse.<TimeSlotsResponseDTO>builder()
                    .success(true)
                    .message("Service not available on " + requestedDay)
                    .data(new TimeSlotsResponseDTO(new ArrayList<>()))
                    .build();
        }

        AvailableTime availableTime = daySlot.get();

        // Check if service is closed on this day
        if (Boolean.TRUE.equals(availableTime.getIsClosed())) {
            return APIResponse.<TimeSlotsResponseDTO>builder()
                    .success(true)
                    .message("Service is closed on " + requestedDay)
                    .data(new TimeSlotsResponseDTO(new ArrayList<>()))
                    .build();
        }

        // Generate all possible time slots for the day
        LocalTime serviceOpenTime = availableTime.getOpenTime();
        LocalTime serviceCloseTime = availableTime.getCloseTime();

        if (Boolean.TRUE.equals(availableTime.getIs24Hours())) {
            serviceOpenTime = LocalTime.MIDNIGHT;
            serviceCloseTime = LocalTime.of(23, 59);
        }

        Integer slotDuration = config.getSlotDuration();
        if (slotDuration == null || slotDuration <= 0) {
            throw new BadRequestException("Invalid or missing slot duration in booking configuration");
        }

        // Generate all possible time slots
        List<TimeSlotsRequestDTO> allPossibleSlots = generateTimeSlotsAsObjects(serviceOpenTime, serviceCloseTime, slotDuration, availableTime, config);

        // Get existing bookings for the requested day
        List<Booking> existingBookings = bookingRepository.findBookingsOnADay(requestedDate, serviceId, BookingStatus.BOOKED);

        // Filter out slots that would exceed capacity if this booking was added
        List<TimeSlotsRequestDTO> availableSlots = new ArrayList<>();
        
        for (TimeSlotsRequestDTO slot : allPossibleSlots) {
            if (isSlotAvailableForBooking(slot, existingBookings, config, availabilityDto)) {
                availableSlots.add(slot);
            }
        }

        TimeSlotsResponseDTO response = new TimeSlotsResponseDTO(availableSlots);

        return APIResponse.<TimeSlotsResponseDTO>builder()
                .success(true)
                .message("Available time slots retrieved successfully")
                .data(response)
                .build();
    }

    /**
     * Helper method to generate time slots as TimeSlotsRequestDTO objects
     * Excludes slots that overlap with break times and includes buffer time between slots
     */
    private List<TimeSlotsRequestDTO> generateTimeSlotsAsObjects(LocalTime openTime, LocalTime closeTime, Integer minutesPerSlot, AvailableTime availableTime, BookingConfiguration config) {
        log.info("Generating time slots from {} to {} with {} minute duration", openTime, closeTime, minutesPerSlot);
        
        if (minutesPerSlot <= 0) {
            throw new RuntimeException("Minutes per slot duration should be greater than 0");
        }

        Duration slotDuration = Duration.ofHours(minutesPerSlot / 60).plusMinutes(minutesPerSlot % 60);
        
        // Get buffer time from configuration (default to 0 if not specified)
        Integer bufferTimeMinutes = config.getBufferTime() != null ? config.getBufferTime() : 0;
        Duration bufferTime = Duration.ofMinutes(bufferTimeMinutes);
        
        log.info("Using buffer time: {} minutes", bufferTimeMinutes);
        
        List<TimeSlotsRequestDTO> slots = new ArrayList<>();
        LocalTime current = openTime;
        int safetyCounter = 0;
        final int MAX_SLOTS = 48; // Maximum for 30-minute slots in 24 hours

        while (!current.plus(slotDuration).isAfter(closeTime) 
                && safetyCounter++ < MAX_SLOTS && current.isBefore(closeTime)) {

            LocalTime end = current.plus(slotDuration);
            
            // Check if this slot overlaps with any break time
            if (!overlapsWithBreakTime(current, end, availableTime.getBreakTimes())) {
                slots.add(new TimeSlotsRequestDTO(current, end));
                log.debug("Added slot: {} - {}", current, end);
                
                // Move to next potential slot start time = current slot end + buffer time
                current = end.plus(bufferTime);
            } else {
                log.debug("Skipping slot {} - {} due to break time overlap", current, end);
                
                // When overlapping with break time, find the earliest break end time that affects this slot
                LocalTime earliestBreakEnd = findEarliestBreakEndTime(current, end, availableTime.getBreakTimes());
                if (earliestBreakEnd != null) {
                    // Start next slot right after the break ends
                    current = earliestBreakEnd;
                } else {
                    // If no specific break end found, advance by minimum increment
                    current = current.plusMinutes(30); // Advance by 30 minutes
                }
            }

            if (safetyCounter >= MAX_SLOTS) {
                throw new IllegalStateException("Possible infinite loop detected in slot generation");
            }
        }

        log.info("Generated {} time slots (excluding break times, including buffer time)", slots.size());
        return slots;
    }

    /**
     * Helper method to check if a time slot overlaps with any break time
     * Uses proper interval overlap detection: two intervals overlap if they intersect at any point
     */
    private boolean overlapsWithBreakTime(LocalTime slotStart, LocalTime slotEnd, List<BreakTime> breakTimes) {
        if (breakTimes == null || breakTimes.isEmpty()) {
            return false;
        }
        
        for (BreakTime breakTime : breakTimes) {
            LocalTime breakStart = breakTime.getBreakStart();
            LocalTime breakEnd = breakTime.getBreakEnd();
            
            if (breakStart == null || breakEnd == null) {
                continue; // Skip invalid break times
            }
            
            // Two intervals [a,b] and [c,d] overlap if: max(a,c) < min(b,d)
            // Or equivalently: !(b <= c || d <= a)
            // In our case: !(slotEnd <= breakStart || breakEnd <= slotStart)
            if (!(slotEnd.isBefore(breakStart) || slotEnd.equals(breakStart) || 
                  breakEnd.isBefore(slotStart) || breakEnd.equals(slotStart))) {
                log.debug("Slot [{} - {}] overlaps with break time [{} - {}]", 
                         slotStart, slotEnd, breakStart, breakEnd);
                return true; // There is an overlap
            }
        }
        
        return false; // No overlap with any break time
    }

    /**
     * Helper method to find the earliest break end time that affects a given time slot
     * @param slotStart the start time of the slot
     * @param slotEnd the end time of the slot
     * @param breakTimes list of break times
     * @return the earliest break end time that overlaps with the slot, or null if none
     */
    private LocalTime findEarliestBreakEndTime(LocalTime slotStart, LocalTime slotEnd, List<BreakTime> breakTimes) {
        if (breakTimes == null || breakTimes.isEmpty()) {
            return null;
        }
        
        LocalTime earliestBreakEnd = null;
        
        for (BreakTime breakTime : breakTimes) {
            LocalTime breakStart = breakTime.getBreakStart();
            LocalTime breakEnd = breakTime.getBreakEnd();
            
            if (breakStart == null || breakEnd == null) {
                continue; // Skip invalid break times
            }
            
            // Check if this break time overlaps with the slot
            if (!(slotEnd.isBefore(breakStart) || slotEnd.equals(breakStart) || 
                  breakEnd.isBefore(slotStart) || breakEnd.equals(slotStart))) {
                // This break overlaps with the slot
                if (earliestBreakEnd == null || breakEnd.isBefore(earliestBreakEnd)) {
                    earliestBreakEnd = breakEnd;
                }
            }
        }
        
        return earliestBreakEnd;
    }

    /**
     * Helper method to check if a time slot is available for the requested booking
     * considering capacity constraints
     */
    private boolean isSlotAvailableForBooking(TimeSlotsRequestDTO slot, 
                                            List<Booking> existingBookings, 
                                            BookingConfiguration config, 
                                            AvailabilityDto availabilityDto) {
        
        // Find overlapping bookings with this slot
        List<Booking> overlappingBookings = existingBookings.stream()
                .filter(booking -> {
                    LocalTime bookingStart = booking.getStartDateTime().toLocalTime();
                    LocalTime bookingEnd = booking.getEndDateTime().toLocalTime();
                    
                    // Check if there's any overlap between the slot and the booking
                    // Allow touching times (end = start), only real overlaps should count
                    return !(slot.getSlotEndTime().isBefore(bookingStart) || slot.getSlotEndTime().equals(bookingStart) || 
                             slot.getSlotStartTime().isAfter(bookingEnd) || slot.getSlotStartTime().equals(bookingEnd));
                })
                .collect(Collectors.toList());

        // Calculate current capacity usage for this slot
        int currentAdultCount = overlappingBookings.stream()
                .mapToInt(booking -> {
                    Integer adults = booking.getTripItem().getNumberOfAdults();
                    return adults != null ? adults : 0;
                })
                .sum();
        
        int currentChildCount = overlappingBookings.stream()
                .mapToInt(booking -> {
                    Integer children = booking.getTripItem().getNumberOfChildren();
                    return children != null ? children : 0;
                })
                .sum();

        int currentUnitCount = overlappingBookings.stream()
                .mapToInt(booking -> {
                    Integer units = booking.getTripItem().getNoOfUnits();
                    return units != null ? units : 1;
                })
                .sum();

        // Calculate total capacity based on configuration
        int totalAdultCapacity = 0;
        int totalChildCapacity = 0;
        
        if (config.getTotalUnits() != null && config.getUnitAdultCapacity() != null) {
            totalAdultCapacity = config.getTotalUnits() * config.getUnitAdultCapacity();
        }
        
        if (config.getTotalUnits() != null && config.getUnitChildCapacity() != null) {
            totalChildCapacity = config.getTotalUnits() * config.getUnitChildCapacity();
        }

        // Add extra capacity if allowed
        if (Boolean.TRUE.equals(config.getAllowExtraCapacity())) {
            if (config.getExtraAdultCapacity() != null) {
                totalAdultCapacity += config.getExtraAdultCapacity();
            }
            if (config.getExtraChildCapacity() != null) {
                totalChildCapacity += config.getExtraChildCapacity();
            }
        }

        // Check if the new booking would fit
        Integer adultCountInteger = availabilityDto.getAdultCount();
        Integer childCountInteger = availabilityDto.getChildCount();
        Integer unitCountInteger = availabilityDto.getNoOfUnits();
        
        int requestedAdults = adultCountInteger != null ? adultCountInteger : 0;
        int requestedChildren = childCountInteger != null ? childCountInteger : 0;
        int requestedUnits = unitCountInteger != null ? unitCountInteger : 1;

        // Check unit capacity constraints
        if (config.getTotalUnits() != null) {
            if (currentUnitCount + requestedUnits > config.getTotalUnits()) {
                return false; // Not enough units available
            }
        }

        // Check adult capacity
        if (totalAdultCapacity > 0 && currentAdultCount + requestedAdults > totalAdultCapacity) {
            return false; // Not enough adult capacity
        }

        // Check child capacity
        if (totalChildCapacity > 0) {
            if (currentChildCount + requestedChildren > totalChildCapacity) {
                return false; // Not enough child capacity
            }
        }

        return true; // Slot is available
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

        return generateTimeSlotsWithBreakTimes(serviceOpenTime, serviceCloseTime, duration, availableTime.getBreakTimes(), config.getBufferTime());
    }
}
