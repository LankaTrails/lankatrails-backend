package com.lankatrails.lankatrails_backend.service.impl;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.lankatrails.lankatrails_backend.dtos.request.BookingRequestDTO;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.BookingResponseDTO;
import com.lankatrails.lankatrails_backend.exception.APIException;
import com.lankatrails.lankatrails_backend.exception.BadCredentialsException;
import com.lankatrails.lankatrails_backend.exception.BadRequestException;
import com.lankatrails.lankatrails_backend.exception.ResourceNotFoundException;
import com.lankatrails.lankatrails_backend.model.Accommodation;
import com.lankatrails.lankatrails_backend.model.AvailabilitySlot;
import com.lankatrails.lankatrails_backend.model.Booking;
import com.lankatrails.lankatrails_backend.model.Service;
import com.lankatrails.lankatrails_backend.model.Tourist;
import com.lankatrails.lankatrails_backend.model.TouristGuide;
import com.lankatrails.lankatrails_backend.model.enums.BookingStatus;
import com.lankatrails.lankatrails_backend.model.enums.BookingType;
import com.lankatrails.lankatrails_backend.model.enums.ServiceCategory;
import com.lankatrails.lankatrails_backend.repositories.AccommodationRepository;
import com.lankatrails.lankatrails_backend.repositories.AvailabilitySlotRepository;
import com.lankatrails.lankatrails_backend.repositories.BookingRepository;
import com.lankatrails.lankatrails_backend.repositories.ServiceRepository;
import com.lankatrails.lankatrails_backend.repositories.TouristGuideRepository;
import com.lankatrails.lankatrails_backend.repositories.TouristRepository;
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
    AuthUtils authUtils;

    @Override
    @Transactional
    public APIResponse<String> checkTimeSlotAvailability(BookingRequestDTO bookingRequestDTO, Long id) {
        // Validate basic input
        APIResponse<String> inputValidation = validateBookingInput(bookingRequestDTO);
        if (!inputValidation.isSuccess()) {
            return inputValidation;
        }

        // Get service once and reuse (fix duplicate service fetching)
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service", id));

        // Get availability slots
        List<AvailabilitySlot> availabilitySlotList = availabilitySlotRepository.findByService_ServiceId(id);
        if (availabilitySlotList.isEmpty()) {
            return createErrorResponse("No availability slots defined for this service");
        }

        // Validate dates and times
        APIResponse<String> dateTimeValidation = validateDateTimeConstraints(bookingRequestDTO, availabilitySlotList);
        if (!dateTimeValidation.isSuccess()) {
            return dateTimeValidation;
        }

        // Validate booking type specific constraints using the already-fetched service
        return validateBookingTypeConstraints(bookingRequestDTO, service, id);
    }

    @Transactional(readOnly = true)
    private APIResponse<String> validateBookingInput(BookingRequestDTO bookingRequestDTO) {
        // Check for null values
        if (bookingRequestDTO.getFromTime() == null || bookingRequestDTO.getToTime() == null || 
            bookingRequestDTO.getAdultCount() == null || bookingRequestDTO.getChildCount() == null) {
            return createErrorResponse("Required booking fields are missing");
        }

        if (bookingRequestDTO.getFromDate() == null || bookingRequestDTO.getToDate() == null) {
            return createErrorResponse("From date and to date are required");
        }

        // Check for negative values
        if (bookingRequestDTO.getAdultCount() < 0 || bookingRequestDTO.getChildCount() < 0) {
            return createErrorResponse("Guest counts cannot be negative");
        }

        // Check for at least one guest
        int totalGuests = bookingRequestDTO.getAdultCount() + bookingRequestDTO.getChildCount();
        if (totalGuests <= 0) {
            return createErrorResponse("At least one guest (adult or child) is required");
        }

        // Time validation - ensure start time is before end time
        if (!bookingRequestDTO.getFromTime().isBefore(bookingRequestDTO.getToTime())) {
            return createErrorResponse("Start time must be before end time");
        }

        return createSuccessResponse("Input validation passed");
    }

    @Transactional(readOnly = true)
    private APIResponse<String> validateDateTimeConstraints(BookingRequestDTO bookingRequestDTO, List<AvailabilitySlot> availabilitySlotList) {
        LocalDate currentDate = LocalDate.now();
        LocalTime requestedStartTime = bookingRequestDTO.getFromTime();
        LocalTime requestedEndTime = bookingRequestDTO.getToTime();
        DayOfWeek requestedStartDay = bookingRequestDTO.getFromDate().getDayOfWeek();
        DayOfWeek requestedEndDay = bookingRequestDTO.getToDate().getDayOfWeek();

        // Validate dates are not in the past
        if (bookingRequestDTO.getFromDate().isBefore(currentDate)) {
            return createErrorResponse("Start date cannot be in the past");
        }
        if (bookingRequestDTO.getToDate().isBefore(currentDate) || 
            bookingRequestDTO.getToDate().isBefore(bookingRequestDTO.getFromDate())) {
            return createErrorResponse("Invalid end date");
        }

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
            LocalTime serviceOpenTime = LocalTime.parse(startDaySlot.getOpenTime());
            LocalTime serviceCloseTime = LocalTime.parse(endDaySlot.getCloseTime());

            if (requestedStartTime.isBefore(serviceOpenTime)) {
                return createErrorResponse("Requested time is before opening time " + serviceOpenTime);
            }
            if (requestedEndTime.isAfter(serviceCloseTime)) {
                return createErrorResponse("Requested time is after closing time " + serviceCloseTime);
            }
            if (requestedStartTime.isAfter(requestedEndTime)) {
                return createErrorResponse("Start time cannot be after end time");
            }
        } catch (Exception e) {
            return createErrorResponse("Invalid time format in availability slots");
        }

        return createSuccessResponse("Date and time validation passed");
    }

    @Transactional(readOnly = true)
    private APIResponse<String> validateBookingTypeConstraints(BookingRequestDTO bookingRequestDTO, Service service, Long serviceId) {
        // Check for tourist's existing bookings in the same slot
        if (hasTouristExistingBooking(bookingRequestDTO)) {
            return createErrorResponse("Item already in the time slot");
        }

        // Check for overlapping bookings in tourist's trip
        if (hasTouristOverlappingBookings(bookingRequestDTO)) {
            return createErrorResponse("Conflicts with the existing bookings in the trip");
        }

        BookingType bookingType = service.getBookingType();
        log.info("Booking Type: {}", bookingType);

        // Validate booking type specific constraints
        if (bookingType == BookingType.TIME_SLOTS) {
            return validateTimeSlotBooking(bookingRequestDTO, serviceId);
        } else if (bookingType == BookingType.ONE_DAY) {
            return validateOneDayBooking(bookingRequestDTO, service, serviceId);
        } else if (bookingType == BookingType.MULTI_DAY) {
            return validateMultiDayBooking(bookingRequestDTO, service, serviceId);
        } else {
            return createErrorResponse("Invalid or unsupported booking type: " + bookingType);
        }
    }

    @Transactional(readOnly = true)
    private boolean hasTouristExistingBooking(BookingRequestDTO bookingRequestDTO) {
        List<Booking> sameSlotTouristBookings = bookingRepository.findByStartTimeAndEndTimeAndFromDateAndToDateAndTourist_UserIdAndBookingStatus(
                bookingRequestDTO.getFromTime(),
                bookingRequestDTO.getToTime(),
                bookingRequestDTO.getFromDate(),
                bookingRequestDTO.getToDate(),
                authUtils.loggedInUserId(),
                BookingStatus.BOOKED
        );
        return !sameSlotTouristBookings.isEmpty();
    }

    @Transactional(readOnly = true)
    private boolean hasTouristOverlappingBookings(BookingRequestDTO bookingRequestDTO) {
        List<Booking> overlapBookings = bookingRepository.findOverlappingBookings(
                authUtils.loggedInUserId(),
                bookingRequestDTO.getFromDate(),
                bookingRequestDTO.getToDate(),
                bookingRequestDTO.getFromTime(),
                bookingRequestDTO.getToTime(),
                BookingStatus.BOOKED
        );
        return !overlapBookings.isEmpty();
    }

    @Transactional(readOnly = true)
    private APIResponse<String> validateTimeSlotBooking(BookingRequestDTO bookingRequestDTO, Long serviceId) {
        // TIME_SLOTS booking must be for the same day
        if (!bookingRequestDTO.getFromDate().equals(bookingRequestDTO.getToDate())) {
            return createErrorResponse("Time slot bookings must be for the same day. From date and to date must be identical.");
        }

        // Check if the specific time slot is already booked
        List<Booking> timeSlotBookings = bookingRepository.findByStartTimeAndEndTimeAndFromDateAndToDateAndService_ServiceIdAndBookingStatus(
                bookingRequestDTO.getFromTime(),
                bookingRequestDTO.getToTime(),
                bookingRequestDTO.getFromDate(),
                bookingRequestDTO.getToDate(),
                serviceId,
                BookingStatus.BOOKED
        );

        if (timeSlotBookings.isEmpty()) {
            return createSuccessResponse("Available for Time-Slot Bookings");
        } else {
            return createErrorResponse("Already booked for this time slot");
        }
    }

    @Transactional(readOnly = true)
    private APIResponse<String> validateOneDayBooking(BookingRequestDTO bookingRequestDTO, Service service, Long serviceId) {
        // ONE_DAY booking must be for the same day
        if (!bookingRequestDTO.getFromDate().equals(bookingRequestDTO.getToDate())) {
            return createErrorResponse("One day bookings must be for the same day. From date and to date must be identical.");
        }

        Optional<Booking> oneDayConflict = bookingRepository.findByFromDateAndService_ServiceIdAndBookingStatus(
                bookingRequestDTO.getFromDate(), serviceId, BookingStatus.BOOKED);

        if (oneDayConflict.isEmpty()) {
            return validateServiceCapacity(bookingRequestDTO, service, serviceId, "One-Day");
        } else {
            return createErrorResponse("Already booked for one-day bookings");
        }
    }

    @Transactional(readOnly = true)
    private APIResponse<String> validateMultiDayBooking(BookingRequestDTO bookingRequestDTO, Service service, Long serviceId) {
        List<Booking> conflictingBookings = bookingRepository.findExactConflictingBookings(
                serviceId,
                bookingRequestDTO.getFromDate(),
                bookingRequestDTO.getFromTime(),
                bookingRequestDTO.getToDate(),
                bookingRequestDTO.getToTime(),
                BookingStatus.BOOKED
        );

        int totalHeads = bookingRequestDTO.getChildCount() + bookingRequestDTO.getAdultCount();

        if (!conflictingBookings.isEmpty()) {
            // Calculate total heads including existing bookings
            for (Booking conflictBooking : conflictingBookings) {
                totalHeads += conflictBooking.getAdults() + conflictBooking.getChildren();
            }
            return checkServiceCategoryCapacity(service, serviceId, totalHeads);
        } else {
            return validateServiceCapacity(bookingRequestDTO, service, serviceId, "Multi-Day");
        }
    }

    @Transactional(readOnly = true)
    private APIResponse<String> validateServiceCapacity(BookingRequestDTO bookingRequestDTO, Service service, Long serviceId, String bookingTypeDesc) {
        Integer totalHeads = bookingRequestDTO.getChildCount() + bookingRequestDTO.getAdultCount();

        if (service.getCategory() != null && ServiceCategory.ACCOMMODATION.equals(service.getCategory().getCategoryName())) {
            Accommodation accommodation = accommodationRepository.findByServiceId(serviceId)
                    .orElseThrow(() -> new ResourceNotFoundException("Accommodation", serviceId));
            if (accommodation.getMaxGuests() < totalHeads) {
                return createErrorResponse("Amount of maximum guests exceeded");
            } else {
                return createSuccessResponse("Available for " + bookingTypeDesc + " accommodation booking");
            }
        }
        return createSuccessResponse("Available for " + bookingTypeDesc + " bookings");
    }

    @Transactional(readOnly = true)
    private APIResponse<String> checkServiceCategoryCapacity(Service service, Long serviceId, Integer totalHeads) {
        switch (service.getCategory().getCategoryName()) {
            case ACCOMMODATION:
                Accommodation accommodation = accommodationRepository.findByServiceId(serviceId)
                        .orElseThrow(() -> new ResourceNotFoundException("Accommodation", serviceId));
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
    //Insert new booking
    public APIResponse<String> addNewBooking(BookingRequestDTO bookingRequestDTO, Long id) {
        APIResponse<String> availabilityResponse=checkTimeSlotAvailability(bookingRequestDTO,id);
        if (availabilityResponse.isSuccess()){
            Booking prepareBooking = new Booking();
            Service service = serviceRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("Service",id));

            Tourist tourist = touristRepository.findByUserId(authUtils.loggedInUserId()).orElseThrow(
                    ()->new ResourceNotFoundException("Tourist", authUtils.loggedInUserId())
            );

            prepareBooking.setService(service);
            prepareBooking.setAdults(bookingRequestDTO.getAdultCount());
            prepareBooking.setChildren(bookingRequestDTO.getChildCount());
            prepareBooking.setEndTime(bookingRequestDTO.getToTime());
            prepareBooking.setStartTime(bookingRequestDTO.getFromTime());
            prepareBooking.setTourist(tourist);
            prepareBooking.setBookingStatus(bookingRequestDTO.getBookingStatus());
            prepareBooking.setFromDate(bookingRequestDTO.getFromDate());
            prepareBooking.setToDate(bookingRequestDTO.getToDate());
            prepareBooking.setBookingStatus(BookingStatus.BOOKED);

            bookingRepository.save(prepareBooking);
            return APIResponse.<String>builder()
                    .success(true)
                    .message("Booking Placed Successfully")
                    .data("")
                    .build();
        }

        return APIResponse.<String>builder()
                .success(false)
                .message(availabilityResponse.getMessage())
                .data("")
                .build();
    }

    //find the bookings a service has on a particular day
    @Override
    @Transactional(readOnly = true)
    public APIResponse<BookingResponseDTO> getBookingsOnTheDay(BookingRequestDTO bookingRequestDTO,Long id){
        List<Booking> bookings = bookingRepository.findBookingsOnADay(bookingRequestDTO.getFromDate(),id,BookingStatus.BOOKED);
        List<BookingRequestDTO> prepareResponse = new ArrayList<>();
        for (Booking booking : bookings){
            //map each to BookingRequestDTO
            BookingRequestDTO setResponse = new BookingRequestDTO();
            setResponse.setFromDate(booking.getFromDate());
//            setResponse.setBookingStatus(booking.getBookingStatus());
            setResponse.setToDate(booking.getToDate());
            setResponse.setAdultCount(booking.getAdults());
            setResponse.setChildCount(booking.getChildren());
            setResponse.setFromTime(booking.getStartTime());
            setResponse.setToTime(booking.getEndTime());

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
        LocalTime serviceOpenTime = LocalTime.parse(startDaySlot.get().getOpenTime());

        //Get the close time
        LocalTime serviceCloseTime = LocalTime.parse(endDaySlot.get().getCloseTime());

        //Get the duration one guiding
        Long duration = touristGuide.getDuration();

        APIResponse<List<String>> timeSlotsResponse = generateTimeSlots(serviceOpenTime,serviceCloseTime,duration);

        return timeSlotsResponse;
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
    public APIResponse<List<String>> getAllFreeTimeSlots(BookingRequestDTO bookingRequestDTO, Long id) {
        //check the availability
        APIResponse<String> availabilityCheck = checkTimeSlotAvailability(bookingRequestDTO,id);
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
                APIResponse<BookingResponseDTO> allBookingsOnTheDay = getBookingsOnTheDay(bookingRequestDTO,id);
                BookingResponseDTO bookingResponseDTO = allBookingsOnTheDay.getData();
                List<BookingRequestDTO> bookingResponseList = bookingResponseDTO.getContent();
                for (BookingRequestDTO bookingRequest : bookingResponseList){
                    slots.add(String.format("%02d:%02d - %02d:%02d",
                            bookingRequest.getFromTime().getHour(), bookingRequest.getFromTime().getMinute(),
                            bookingRequest.getToTime().getHour(), bookingRequest.getToTime().getMinute()
                            ));
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
        LocalTime serviceOpenTime = LocalTime.parse(startDaySlot.get().getOpenTime());

        //Get the close time
        LocalTime serviceCloseTime = LocalTime.parse(endDaySlot.get().getCloseTime());

        //Get the duration one guiding
        Long duration = service.getDuration();
        APIResponse<List<String>> timeSlotsResponse = generateTimeSlots(serviceOpenTime,serviceCloseTime,duration);



        return timeSlotsResponse;
    }


}
