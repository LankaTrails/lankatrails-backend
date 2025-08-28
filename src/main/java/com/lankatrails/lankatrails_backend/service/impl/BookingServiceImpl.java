package com.lankatrails.lankatrails_backend.service.impl;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.lankatrails.lankatrails_backend.dtos.AvailabilityDto;
import com.lankatrails.lankatrails_backend.exception.*;
import com.lankatrails.lankatrails_backend.model.*;
import com.lankatrails.lankatrails_backend.model.enums.TripPrivilege;
import com.lankatrails.lankatrails_backend.repositories.*;
import com.lankatrails.lankatrails_backend.service.utils.TripPrivilegeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.lankatrails.lankatrails_backend.dtos.request.BookingRequestDTO;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.BookingResponseDTO;
import com.lankatrails.lankatrails_backend.model.enums.BookingStatus;
import com.lankatrails.lankatrails_backend.model.enums.BookingType;
import com.lankatrails.lankatrails_backend.model.enums.ServiceCategory;
import com.lankatrails.lankatrails_backend.security.utils.AuthUtils;
import com.lankatrails.lankatrails_backend.service.BookingService;

import lombok.extern.slf4j.Slf4j;

@org.springframework.stereotype.Service
@Slf4j
public class BookingServiceImpl implements BookingService {
    @Autowired
    AvailabilitySlotRepository availabilitySlotRepository;

    @Autowired
    BookingRepository bookingRepository;

    @Autowired
    ServiceRepository serviceRepository;

    @Autowired
    AccommodationRepository accommodationRepository;

    @Autowired
    TouristRepository touristRepository;

    @Autowired
    TouristGuideRepository touristGuideRepository;

    @Autowired
    TripParticipantRepository tripParticipantRepository;

    @Autowired
    TripItemRepository tripItemRepository;

    @Autowired
    TripRepository tripRepository;

    @Autowired
    TripPrivilegeUtils tripPrivilegeUtils;

    @Autowired
    AuthUtils authUtils;

    @Override
    @Transactional
    public APIResponse<String> checkAvailability(AvailabilityDto availabilityDto) {
        // Validate basic input
        APIResponse<String> inputValidation = validateAvailabilityInput(availabilityDto);
        if (!inputValidation.isSuccess()) {
            return inputValidation;
        }
        // Validate dates and times within service constraints
        APIResponse<String> dateTimeValidation = validateDateTimeConstraints(availabilityDto);
        if (!dateTimeValidation.isSuccess()) {
            return dateTimeValidation;
        }
        // Validate service availability
        return validateServiceAvailability(availabilityDto);
    }

    @Override
    @Transactional(readOnly = true)
    public APIResponse<String> validateAvailabilityInput(AvailabilityDto availabilityDto) {
        // Check for null values
        if (availabilityDto.getStartDateTime() == null || availabilityDto.getEndDateTime() == null ||
            availabilityDto.getAdultCount() == null || availabilityDto.getChildCount() == null) {
            return createErrorResponse("Required fields are missing");
        }

        // Check for negative values
        if (availabilityDto.getAdultCount() < 0 || availabilityDto.getChildCount() < 0) {
            return createErrorResponse("Guest counts cannot be negative");
        }

        // Check for at least one guest
        int totalGuests = availabilityDto.getAdultCount() + availabilityDto.getChildCount();
        if (totalGuests <= 0) {
            return createErrorResponse("At least one guest (adult or child) is required");
        }

        //validate the past check
        if (availabilityDto.getStartDateTime().isBefore(LocalDateTime.now()) || availabilityDto.getEndDateTime().isBefore(LocalDateTime.now())) {
            return createErrorResponse("Booking dates cannot be in the past");
        }

        // Time validation - ensure start time is before end time
        if (!availabilityDto.getStartDateTime().isBefore(availabilityDto.getEndDateTime())) {
            return createErrorResponse("Start time must be before end time");
        }

        return createSuccessResponse("Input validation passed");
    }

    @Override
    @Transactional(readOnly = true)
    public APIResponse<String> validateDateTimeConstraints(AvailabilityDto availabilityDto) {
        // Get availability slots
        List<AvailabilitySlot> availabilitySlotList = availabilitySlotRepository.findByService_ServiceId(availabilityDto.getServiceId());
        if (availabilitySlotList.isEmpty()) {
            return createErrorResponse("No availability slots defined for this service" + availabilityDto.getServiceId());
        }

        LocalDateTime requestedStartDateTime = availabilityDto.getStartDateTime();
        LocalDateTime requestedEndDateTime = availabilityDto.getEndDateTime();
        DayOfWeek requestedStartDay = requestedStartDateTime.getDayOfWeek();
        DayOfWeek requestedEndDay = requestedEndDateTime.getDayOfWeek();

        // Create map for optimized availability slot lookup
        Map<DayOfWeek, AvailabilitySlot> availabilityMap = availabilitySlotList.stream()
                .collect(Collectors.toMap(
                    slot -> DayOfWeek.valueOf(slot.getDayOfWeek().toUpperCase()),
                    Function.identity(),
                    (existing, replacement) -> existing // Keep first occurrence in case of duplicates
                ));

        // Find matching availability slots using map lookup
        AvailabilitySlot startDaySlot = availabilityMap.get(requestedStartDay);
        AvailabilitySlot endDaySlot = availabilityMap.get(requestedEndDay);

        if (startDaySlot == null && endDaySlot == null) {
            return createErrorResponse("Service not available on " + requestedStartDay + " and " + requestedEndDay);
        } else if (endDaySlot == null) {
            return createErrorResponse("Service not available on " + requestedEndDay);
        } else if (startDaySlot == null) {
            return createErrorResponse("Service not available on " + requestedStartDay);
        }

        // Validate time constraints with null safety
        try {
            LocalDate requestDate = requestedStartDateTime.toLocalDate();

            LocalDateTime serviceOpenDateTime = LocalDateTime.of(requestDate, startDaySlot.getOpenTime());
            LocalDateTime serviceCloseDateTime = LocalDateTime.of(requestDate, endDaySlot.getCloseTime());

            if (requestedStartDateTime.isBefore(serviceOpenDateTime)) {
                return createErrorResponse("Requested time is before opening time " + serviceOpenDateTime.toLocalTime());
            }
            if (requestedEndDateTime.isAfter(serviceCloseDateTime)) {
                return createErrorResponse("Requested time is after closing time " + serviceCloseDateTime.toLocalTime());
            }
            if (requestedStartDateTime.isAfter(requestedEndDateTime)) {
                return createErrorResponse("Start time cannot be after end time");
            }

        } catch (Exception e) {
            return createErrorResponse("Invalid time format in availability slots");
        }

        return createSuccessResponse("Date and time validation passed");
    }

    @Override
    @Transactional(readOnly = true)
    public APIResponse<String> validateServiceAvailability(AvailabilityDto availabilityDto) {
        // Get service once and reuse (fix duplicate service fetching)
        Service service = serviceRepository.findById(availabilityDto.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Service", availabilityDto.getServiceId()));

        log.info("Booking Type: {}", service.getBookingType());

        // Validate booking type specific constraints
        if (service.getBookingType() == BookingType.TIME_SLOTS) {
            return validateTimeSlotBooking(availabilityDto);
        } else if (
                service.getBookingType()== BookingType.MULTI_DAY) {
            return validateMultiDayBooking(availabilityDto);
        } else {
            return createErrorResponse("Invalid or unsupported booking type: " + service.getBookingType());
        }
    }

    @Transactional(readOnly = true)
    private APIResponse<String> validateTimeSlotBooking(AvailabilityDto availabilityDto) {
        // TIME_SLOTS booking must be for the same day
        if (!availabilityDto.getStartDateTime().toLocalDate().equals(availabilityDto.getEndDateTime().toLocalDate())) {
            return createErrorResponse("Time slot bookings must be for the same day. From date and to date must be identical.");
        }

        // Check if the specific time slot is already booked
        List<Booking> timeSlotBookings = bookingRepository.findByStartDateTimeAndEndDateTimeAndTripItem_Service_ServiceIdAndBookingStatus(
                availabilityDto.getStartDateTime(),
                availabilityDto.getEndDateTime(),
                availabilityDto.getServiceId(),
                BookingStatus.BOOKED
        );

        if (timeSlotBookings.isEmpty()) {
            return createSuccessResponse("Available for Time-Slot Bookings");
        } else {
            return createErrorResponse("Already booked for this time slot");
        }
    }

    @Transactional(readOnly = true)
    private APIResponse<String> validateMultiDayBooking(AvailabilityDto availabilityDto) {
        List<Booking> conflictingBookings = bookingRepository.findExactConflictingBookings(
                availabilityDto.getServiceId(),
                availabilityDto.getStartDateTime(),
                availabilityDto.getEndDateTime(),
                BookingStatus.BOOKED
        );

        if (!conflictingBookings.isEmpty()) {
            return createErrorResponse("Already booked for this period");
        } else {
            return createSuccessResponse("Available for Multi-Day Bookings");
        }
    }

    @Transactional(readOnly = true)
    private APIResponse<String> validateServiceCapacity(AvailabilityDto availabilityDto, Service service, BookingType bookingType) {
        Integer totalHeads = availabilityDto.getChildCount() + availabilityDto.getAdultCount();

        if (service.getCategory() != null && ServiceCategory.ACCOMMODATION.equals(service.getCategory().getCategoryName())) {
            Accommodation accommodation = accommodationRepository.findByServiceId(service.getServiceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Accommodation", service.getServiceId()));
            if (accommodation.getMaxGuests() < totalHeads) {
                return createErrorResponse("Amount of maximum guests exceeded");
            } else {
                return createSuccessResponse("Available for " + bookingType + " accommodation booking");
            }
        }
        return createSuccessResponse("Available for " + bookingType + " bookings");
    }

    @Transactional(readOnly = true)
    private APIResponse<String> checkServiceCategoryCapacity(Service service, Integer totalHeads) {
        switch (service.getCategory().getCategoryName()) {
            case ACCOMMODATION:
                Accommodation accommodation = accommodationRepository.findByServiceId(service.getServiceId())
                        .orElseThrow(() -> new ResourceNotFoundException("Accommodation", service.getServiceId()));
                if (accommodation.getMaxGuests() < totalHeads) {
                    return createErrorResponse("Amount of maximum guests exceeded");
                }
                break;
            case FOOD_BEVERAGE:
            case ACTIVITY:
            case TRANSPORT:
            case TOUR_GUIDE:
                return createErrorResponse("Already booked");
            default:
                return createErrorResponse("Unsupported service category");
        }
        return createSuccessResponse("Available for booking");
    }

    private APIResponse<String> createErrorResponse(String message) {
        return APIResponse.<String>builder()
                .success(false)
                .message(message)
                .data("")
                .build();
    }

    private APIResponse<String> createSuccessResponse(String message) {
        return APIResponse.<String>builder()
                .success(true)
                .message(message)
                .data("")
                .build();
    }

    @Override
    @Transactional
    public APIResponse<String> addNewBooking(Long tripItemId) {
        TripParticipant tripParticipant = tripParticipantRepository.findByTourist_UserId(authUtils.loggedInUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Trip Participant", authUtils.loggedInUserId()));

        // Validate participant's privilege to book for the trip
        if (!tripPrivilegeUtils.hasPrivilege(tripParticipant.getTripRole(), TripPrivilege.ADD_BOOKINGS)) {
            throw new BadRequestException("Insufficient privileges to add bookings to this trip");
        }

        TripItem tripItem = tripItemRepository.findById(tripItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip Item", tripItemId));

        AvailabilityDto availabilityDto = AvailabilityDto.builder()
                .startDateTime(tripItem.getStartTime())
                .endDateTime(tripItem.getEndTime())
                .adultCount(tripItem.getTrip().getNumberOfAdults())
                .childCount(tripItem.getTrip().getNumberOfChildren())
                .serviceId(tripItem.getService().getServiceId())
                .tripId(tripItem.getTrip().getTripId())
                .build();

        APIResponse<String> availabilityResponse = validateServiceAvailability(availabilityDto);

        if (availabilityResponse.isSuccess()) {

            Booking newBooking = Booking.builder()
                    .startDateTime(tripItem.getStartTime())
                    .endDateTime(tripItem.getEndTime())
                    .tripItem(tripItem)
                    .tripParticipant(tripParticipant)
                    .bookedDateTime(LocalDate.now().atTime(LocalTime.now()))
                    .bookingStatus(BookingStatus.BOOKED)
                    .build();

            bookingRepository.save(newBooking);

            return createSuccessResponse("Booking added successfully");
        }

        return availabilityResponse;
    }

    //find the bookings a service has on a particular day
    @Override
    @Transactional(readOnly = true)
    public APIResponse<BookingResponseDTO> getBookingsOnTheDay(AvailabilityDto availabilityDto,Long id){
        List<Booking> bookings = bookingRepository.findBookingsOnADay(availabilityDto.getStartDateTime().toLocalDate(),id,BookingStatus.BOOKED);
        List<BookingRequestDTO> prepareResponse = new ArrayList<>();
        for (Booking booking : bookings){
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
        return  APIResponse.<BookingResponseDTO>builder()
                .success(true)
                .message("")
                .data(responseDTO)
                .build();
    }

    @Override
    @Transactional
    //find the available slots of the tour guide
    public APIResponse<List<String>> getTourGuideDaySlots(Long id) {

        //Find the tour guide
        TouristGuide touristGuide = touristGuideRepository.findByServiceId(id).orElseThrow(
                ()->new ResourceNotFoundException("Tour Guide",id)
        );
        //Get the current date
        LocalDate currentDate = LocalDate.now();
        //Get the day of the week which the current day belongs
        List<AvailabilitySlot> availabilitySlotList = availabilitySlotRepository.findByService_ServiceId(id);
        if (availabilitySlotList.isEmpty()){
            throw new BadRequestException("No Availability Slots Defined");
        }
        DayOfWeek requestedStartDay = currentDate.getDayOfWeek();
        DayOfWeek requestedEndDay = currentDate.getDayOfWeek();
        Optional<AvailabilitySlot> startDaySlot = availabilitySlotList.stream()
                .filter(slot -> slot.getDayOfWeek().equalsIgnoreCase(requestedStartDay.toString()))
                .findFirst();
        Optional<AvailabilitySlot> endDaySlot = availabilitySlotList.stream()
                .filter(slot -> slot.getDayOfWeek().equalsIgnoreCase(requestedEndDay.toString()))
                .findFirst();

        //Get the open time
        LocalTime serviceOpenTime = startDaySlot.get().getOpenTime();

        //Get the close time
        LocalTime serviceCloseTime = endDaySlot.get().getCloseTime();

        //Get the duration one guiding
        Long duration = touristGuide.getDuration();

        return generateTimeSlots(serviceOpenTime,serviceCloseTime,duration);
    }

    @Override
    @Transactional(readOnly = true)
    //generating all the time slots based on the open time, close time and duration of one slot
    public APIResponse<List<String>> generateTimeSlots(LocalTime openTime, LocalTime closeTime, Long hoursPerSlot) {
        log.info("hours per slot" +hoursPerSlot);
        // 1. Validate input
        if (hoursPerSlot <= 0) {
            throw new APIException("Hours per slot duration should be greater than 0");
        }

        // 2. Create duration (ensure this shows PT2H in logs for 2 hours)
        Duration slotDuration = Duration.ofHours(hoursPerSlot);
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
    public APIResponse<List<String>> getAllFreeTimeSlots(AvailabilityDto availabilityDto, Long id) {
        //check the availability
        APIResponse<String> availabilityCheck = checkAvailability(availabilityDto);
        //if available, load all the free slots on the particular day it is selected
        if (availabilityCheck.isSuccess()){
            //Find the service
            Service service = serviceRepository.findById(id)
                    .orElseThrow(()-> new ResourceNotFoundException("Service",id));
            BookingType bookingType = service.getBookingType();

            //load the available free slots
            List<String> slots = new ArrayList<>();
            if(bookingType == BookingType.TIME_SLOTS ){
                //load the free time slots that are not booked on the particular date
                //get all the time slots
                APIResponse<List<String>> allTimeSlots = getServiceTimeSlots(id);
                List<String> allTimeSlotsList = allTimeSlots.getData();

                //check bookings received on those time slots
                APIResponse<BookingResponseDTO> allBookingsOnTheDay = getBookingsOnTheDay(availabilityDto,id);
                BookingResponseDTO bookingResponseDTO = allBookingsOnTheDay.getData();
                List<BookingRequestDTO> bookingResponseList = bookingResponseDTO.getContent();
                for (BookingRequestDTO bookingRequest : bookingResponseList){
                    String slot = String.format("%02d:%02d - %02d:%02d",
                            bookingRequest.getStartDateTime().getHour(), bookingRequest.getStartDateTime().getMinute(),
                            bookingRequest.getEndDateTime().getHour(), bookingRequest.getEndDateTime().getMinute());
                    slots.add(slot);
                }
                //remove the bookings received time slots
                List<String> availableForBookingSlots = allTimeSlotsList.stream()
                        .filter(item -> !slots.contains(item))
                        .collect(Collectors.toList());

                return APIResponse.<List<String>>builder()
                        .success(true)
                        .message("Available slots for booking")
                        .data(availableForBookingSlots)
                        .build();

            }else{
                return APIResponse.<List<String>>builder()
                        .success(true)
                        .message(availabilityCheck.getMessage())
                        .data(new ArrayList<>())
                        .build();
            }
//            else if (bookingType == MULTI_DAY) {
//                return APIResponse.<List<String>>builder()
//                        .success(true)
//                        .message("Available slots for booking")
//                        .data(availableForBookingSlots)
//                        .build();
//            } else if(bookingType == ONE_DAY){
//
//            }

        }else{
            return APIResponse.<List<String>>builder()
                    .success(true)
                    .message(availabilityCheck.getMessage())
                    .data(new ArrayList<>())
                    .build();
        }

    }

    //generate the timeslots available for a day
    @Override
    @Transactional(readOnly = true)
    public APIResponse<List<String>> getServiceTimeSlots (Long id){
        Service service = serviceRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Service",id));
        //Get the current date
        LocalDate currentDate = LocalDate.now();
        //Get the day of the week which the current day belongs
        List<AvailabilitySlot> availabilitySlotList = availabilitySlotRepository.findByService_ServiceId(id);
        if (availabilitySlotList.isEmpty()){
            throw new BadCredentialsException("Invalid Availability Slots","No Availability Slots Defined");
        }
        DayOfWeek requestedStartDay = currentDate.getDayOfWeek();
        DayOfWeek requestedEndDay = currentDate.getDayOfWeek();
        Optional<AvailabilitySlot> startDaySlot = availabilitySlotList.stream()
                .filter(slot -> slot.getDayOfWeek().equalsIgnoreCase(requestedStartDay.toString()))
                .findFirst();
        Optional<AvailabilitySlot> endDaySlot = availabilitySlotList.stream()
                .filter(slot -> slot.getDayOfWeek().equalsIgnoreCase(requestedEndDay.toString()))
                .findFirst();

        //Get the open time
        LocalTime serviceOpenTime = startDaySlot.get().getOpenTime();

        //Get the close time
        LocalTime serviceCloseTime = endDaySlot.get().getCloseTime();

        //Get the duration one guiding
        Long duration = service.getDuration();
        APIResponse<List<String>> timeSlotsResponse = generateTimeSlots(serviceOpenTime,serviceCloseTime,duration);



        return timeSlotsResponse;
    }


}
