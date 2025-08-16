package com.lankatrails.lankatrails_backend.service.impl;

import com.lankatrails.lankatrails_backend.dtos.request.BookingRequestDTO;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.BookingResponseDTO;
import com.lankatrails.lankatrails_backend.exception.APIException;
import com.lankatrails.lankatrails_backend.exception.BadCredentialsException;
import com.lankatrails.lankatrails_backend.exception.ResourceNotFoundException;
import com.lankatrails.lankatrails_backend.model.*;
import com.lankatrails.lankatrails_backend.model.enums.BookingStatus;
import com.lankatrails.lankatrails_backend.model.enums.BookingType;
import com.lankatrails.lankatrails_backend.model.enums.ServiceCategory;
import com.lankatrails.lankatrails_backend.repositories.*;
import com.lankatrails.lankatrails_backend.security.utils.AuthUtils;
import com.lankatrails.lankatrails_backend.service.BookingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.lankatrails.lankatrails_backend.model.enums.BookingType.*;

@Service
@Slf4j
public class BookingServiceImpl implements BookingService {
    @Autowired
    AvailabilitySlotRepository availabilitySlotRepository;

    @Autowired
    BookingRepository bookingRepository;

    @Autowired
    ServiceRepository serviceRepository;

    @Autowired
    FoodBeverageRepository foodBeverageRepository;

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
    //checking the availability of the time slot based on the booking type of the service
    public APIResponse<String> checkTimeSlotAvailability(BookingRequestDTO bookingRequestDTO, Long id) {
        List<AvailabilitySlot> availabilitySlotList = availabilitySlotRepository.findByService_ServiceId(id);
        if (availabilitySlotList.isEmpty()){
            throw new BadCredentialsException("Invalid Availability Slots","No Availability Slots Defined");
        }

        //Parse requested time slot
        // Convert DTO times to match database format
        LocalTime requestedStartTime = LocalTime.parse(bookingRequestDTO.getFromTime() + ":00");
        LocalTime requestedEndTime = LocalTime.parse(bookingRequestDTO.getToTime() + ":00");
        DayOfWeek requestedStartDay = bookingRequestDTO.getFromDate().getDayOfWeek();
        DayOfWeek requestedEndDay = bookingRequestDTO.getToDate().getDayOfWeek();

        //check whether the start date is before the current date
        LocalDate currentDate = LocalDate.now();
        if(bookingRequestDTO.getFromDate().isBefore(currentDate)){
            return APIResponse.<String>builder()
                    .success(false)
                    .message("Start date is invalid")
                    .data("")
                    .build();
        }
        //check whether the start date is before the current date
        if(bookingRequestDTO.getToDate().isBefore(currentDate) || bookingRequestDTO.getToDate().isBefore(bookingRequestDTO.getFromDate())){
            return APIResponse.<String>builder()
                    .success(false)
                    .message("Invalid End Date")
                    .data("")
                    .build();
        }

        //Find matching start day slot
        Optional<AvailabilitySlot> startDaySlot = availabilitySlotList.stream()
                .filter(slot -> slot.getDayOfWeek().equalsIgnoreCase(requestedStartDay.toString()))
                .findFirst();

        //Find matching end day slot
        Optional<AvailabilitySlot> endDaySlot = availabilitySlotList.stream()
                .filter(slot -> slot.getDayOfWeek().equalsIgnoreCase(requestedEndDay.toString()))
                .findFirst();

        if(startDaySlot.isEmpty() && endDaySlot.isEmpty()){
            return APIResponse.<String>builder()
                    .success(false)
                    .message("Service not Available on "+requestedStartDay+" and "+requestedEndDay)
                    .data("")
                    .build();
        }else if(endDaySlot.isEmpty()){
            return APIResponse.<String>builder()
                    .success(false)
                    .message("Service not Available on "+requestedEndDay)
                    .data("")
                    .build();
        }else if(startDaySlot.isEmpty()){
            return APIResponse.<String>builder()
                    .success(false)
                    .message("Service not Available on "+requestedStartDay)
                    .data("")
                    .build();
        }

        //Parse service's open/close times
        AvailabilitySlot startTimeSlot = startDaySlot.get();
        AvailabilitySlot endTimeSlot = endDaySlot.get();

        LocalTime serviceOpenTime = LocalTime.parse(startTimeSlot.getOpenTime());
        LocalTime serviceCloseTime = LocalTime.parse(endTimeSlot.getCloseTime());

        //Perform time validation
        if(requestedStartTime.isBefore(serviceOpenTime)){
            return APIResponse.<String>builder()
                    .success(false)
                    .message("Requested time is before opening time "+serviceOpenTime)
                    .data("")
                    .build();
        }
        if (requestedEndTime.isAfter(serviceCloseTime)){
            return APIResponse.<String>builder()
                    .success(false)
                    .message("Requested time is after closing time "+serviceOpenTime)
                    .data("")
                    .build();
        }
        if (requestedStartTime.isAfter(requestedEndTime)){
            return APIResponse.<String>builder()
                    .success(false)
                    .message("Start time cannot be after end time")
                    .data("")
                    .build();

        }

            //Find the service
            com.lankatrails.lankatrails_backend.model.Service service = serviceRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Service",id));

            //check whether a booking is added on the same time slot in the trip
            List<Booking> sameSlot_touristBookings = bookingRepository.findByStartTimeAndEndTimeAndFromDateAndToDateAndTourist_UserIdAndBookingStatus(
                    requestedStartTime,
                    requestedEndTime,
                    bookingRequestDTO.getFromDate(),
                    bookingRequestDTO.getToDate(),
                    authUtils.loggedInUser().getUserId(),
                    BookingStatus.BOOKED
            );
            BookingType bookingType = service.getBookingType();
            if (sameSlot_touristBookings.isEmpty()){
                    //check whether there are conflicting items in the trip when adding into bookings
                    List<Booking> overlapBookings = bookingRepository.findOverlappingBookings(
                            authUtils.loggedInUserId(),
                            bookingRequestDTO.getFromDate(),
                            bookingRequestDTO.getToDate(),
                            requestedStartTime,
                            requestedEndTime,
                            BookingStatus.BOOKED
                    );
                    //if no conflicting items are available for the booking
                    //validate for the availability of the quantities if available
                    //Get the total head count of the new booking received
                    Integer totalHeads = bookingRequestDTO.getChildCount() + bookingRequestDTO.getAdultCount();
                    if (overlapBookings.isEmpty()){
                        //start checking with the service
                        //trying to place the booking for multiple days
                        log.info("Booking Type"+bookingType);
                        if (bookingType == MULTI_DAY){
                            //Find whether the service will get overlapping bookings because of the new booking
                            List<Booking> conflictingBookings=bookingRepository.findExactConflictingBookings(
                                    id,
                                    bookingRequestDTO.getFromDate(),
                                    requestedStartTime,
                                    bookingRequestDTO.getToDate(),
                                    requestedEndTime,
                                    BookingStatus.BOOKED
                            );


                            //accommodation, food-beverage can have many tourists at once
                            //therefore should check whether the newly received booking exceeds the maximum head count possible in the given range
                            if (!conflictingBookings.isEmpty()){
                                //get the total head count of existing bookings and the newly received booking
                                for (Booking conflictBooking : conflictingBookings){
                                    totalHeads = totalHeads +conflictBooking.getAdults()+conflictBooking.getChildren();
                                }

                                //for accommodation
                                if (service.getCategory().getCategoryName() == ServiceCategory.ACCOMMODATION){
                                    Accommodation accommodation= accommodationRepository.findByServiceId(id).orElseThrow(()->new ResourceNotFoundException("Accommodation",id));
                                    //exceeds the no of maximum possible guests
                                    if (accommodation.getMaxGuests() <= totalHeads){
                                        return APIResponse.<String>builder()
                                                .success(false)
                                                .message("Amount of maximum guests, exceeded")
                                                .data("")
                                                .build();
                                    }

                                }else if (service.getCategory().getCategoryName() == ServiceCategory.ACTIVITY ||
                                        service.getCategory().getCategoryName() == ServiceCategory.TRANSPORT ||
                                        service.getCategory().getCategoryName() == ServiceCategory.TOUR_GUIDE){
                                    //for activity provider, transport, tour-guide needs to check only whether there is a booking on the newly received dates
                                    return APIResponse.<String>builder()
                                            .success(false)
                                            .message("Already Booked")
                                            .data("")
                                            .build();
                                }


                            }else{
                                if (service.getCategory().getCategoryName() == ServiceCategory.ACCOMMODATION) {
                                    Accommodation accommodation = accommodationRepository.findByServiceId(id).orElseThrow(() -> new ResourceNotFoundException("Accommodation", id));
                                    //exceeds the no of maximum possible guests
                                    if (accommodation.getMaxGuests() <= totalHeads) {
                                        return APIResponse.<String>builder()
                                                .success(false)
                                                .message("Amount of maximum guests, exceeded")
                                                .data("")
                                                .build();
                                    }else{
                                        return APIResponse.<String>builder()
                                                .success(true)
                                                .message("Available for Multi-Day accommodation Booking")
                                                .data("")
                                                .build();
                                    }
                                }else{
                                    return APIResponse.<String>builder()
                                            .success(true)
                                            .message("Available for Multi-DayBookings")
                                            .data("")
                                            .build();
                                }
                            }
                        }
                        //trying to place the booking for a day
                        if (bookingType == ONE_DAY){
                            Optional<Booking> oneDayConflict =bookingRepository.findByFromDateAndService_ServiceIdAndBookingStatus(bookingRequestDTO.getFromDate(),id,BookingStatus.BOOKED);
                            if (oneDayConflict.isEmpty()){
                                if (service.getCategory().getCategoryName() == ServiceCategory.ACCOMMODATION) {
                                    Accommodation accommodation = accommodationRepository.findByServiceId(id).orElseThrow(() -> new ResourceNotFoundException("Accommodation", id));
                                    if (accommodation.getMaxGuests() <= totalHeads) {
                                        return APIResponse.<String>builder()
                                                .success(false)
                                                .message("Amount of maximum guests, exceeded")
                                                .data("")
                                                .build();
                                    }else{
                                        return APIResponse.<String>builder()
                                                .success(true)
                                                .message("Available for One-Day Bookings")
                                                .data("")
                                                .build();
                                    }
                                }else{
                                    return APIResponse.<String>builder()
                                            .success(true)
                                            .message("Available for One-Day Bookings")
                                            .data("")
                                            .build();
                                }
                            }else{
                                return APIResponse.<String>builder()
                                        .success(false)
                                        .message("Already Booked for One-Day bookings")
                                        .data("")
                                        .build();
                            }
                        }
                        //trying to place the booking for time_slots
                        if (bookingType == TIME_SLOTS){
                                List<Booking> timeSlotBookings=bookingRepository.findByStartTimeAndEndTimeAndFromDateAndToDateAndService_ServiceIdAndBookingStatus(
                                        requestedStartTime,
                                        requestedEndTime,
                                        bookingRequestDTO.getFromDate(),
                                        bookingRequestDTO.getToDate(),
                                        id,
                                        BookingStatus.BOOKED
                                );
                                if (timeSlotBookings.isEmpty()){
                                    return APIResponse.<String>builder()
                                            .success(true)
                                            .message("Available for Time-Slot Bookings")
                                            .data("")
                                            .build();
                                }else{
                                    return APIResponse.<String>builder()
                                            .success(false)
                                            .message("Already Booked for Time-Slots bookings")
                                            .data("")
                                            .build();
                                }
                        }
                    }else{
                        return APIResponse.<String>builder()
                                .success(false)
                                .message("Conflicts with the existing bookings in the trip")
                                .data("")
                                .build();
                    }

            }else{
                return APIResponse.<String>builder()
                        .success(false)
                        .message("Item already in the time slot ")
                        .data("")
                        .build();
            }

        return null;
    }

    @Override
    @Transactional
    //Insert new booking
    public APIResponse<String> addNewBooking(BookingRequestDTO bookingRequestDTO, Long id) {
        APIResponse<String> availabilityResponse=checkTimeSlotAvailability(bookingRequestDTO,id);
        if (availabilityResponse.isSuccess()){
            Booking prepareBooking = new Booking();
            com.lankatrails.lankatrails_backend.model.Service service = serviceRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("Service",id));
            ServiceCategory category = service.getCategory().getCategoryName();

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
    public APIResponse<BookingResponseDTO> getBookingsOnTheDay(BookingRequestDTO bookingRequestDTO,Long id){
        List<Booking> bookings = bookingRepository.findBookingsOnADay(bookingRequestDTO.getFromDate(),id);
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
        Long duration = touristGuide.getDuration();
        Duration totalAvailableDuration = Duration.between(serviceOpenTime,serviceCloseTime);
        //no. of slots
//        Long availableSlots = totalAvailableDuration.dividedBy(duration);

        APIResponse<List<String>> timeSlotsResponse = generateTimeSlots(serviceOpenTime,serviceCloseTime,duration);

        return timeSlotsResponse;
    }

    @Override
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
    public APIResponse<List<String>> getAllFreeTimeSlots(BookingRequestDTO bookingRequestDTO, Long id) {
        //check the availability
        APIResponse<String> availabilityCheck = checkTimeSlotAvailability(bookingRequestDTO,id);
        //if available, load all the free slots on the particular day it is selected
        if (availabilityCheck.isSuccess()){
            //Find the service
            com.lankatrails.lankatrails_backend.model.Service service = serviceRepository.findById(id)
                    .orElseThrow(()-> new ResourceNotFoundException("Service",id));
            BookingType bookingType = service.getBookingType();

            //load the available free slots
            List<String> slots = new ArrayList<>();
            if(bookingType == TIME_SLOTS ){
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
                            bookingRequest.getToTime().getHour(),bookingRequest.getToTime().getMinute(),
                            bookingRequest.getFromTime().getHour(), bookingRequest.getFromTime().getMinute()
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
    public APIResponse<List<String>> getServiceTimeSlots (Long id){
        com.lankatrails.lankatrails_backend.model.Service service = serviceRepository.findById(id)
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
