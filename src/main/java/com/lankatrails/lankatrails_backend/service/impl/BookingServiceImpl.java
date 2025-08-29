package com.lankatrails.lankatrails_backend.service.impl;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.lankatrails.lankatrails_backend.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.lankatrails.lankatrails_backend.dtos.AvailabilityDto;
import com.lankatrails.lankatrails_backend.dtos.request.BookingRequestDTO;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.AvailabilityResponse;
import com.lankatrails.lankatrails_backend.dtos.response.BookingResponseDTO;
import com.lankatrails.lankatrails_backend.exception.BadRequestException;
import com.lankatrails.lankatrails_backend.model.AvailableTime;
import com.lankatrails.lankatrails_backend.model.enums.BookingStatus;
import com.lankatrails.lankatrails_backend.model.enums.BookingType;
import com.lankatrails.lankatrails_backend.model.enums.TripPrivilege;
import com.lankatrails.lankatrails_backend.repositories.AvailabilitySlotRepository;
import com.lankatrails.lankatrails_backend.repositories.BookingRepository;
import com.lankatrails.lankatrails_backend.repositories.ServiceRepository;
import com.lankatrails.lankatrails_backend.repositories.TouristGuideRepository;
import com.lankatrails.lankatrails_backend.repositories.TripItemRepository;
import com.lankatrails.lankatrails_backend.repositories.TripParticipantRepository;
import com.lankatrails.lankatrails_backend.security.utils.AuthUtils;
import com.lankatrails.lankatrails_backend.service.AvailabilityService;
import com.lankatrails.lankatrails_backend.service.BookingService;
import com.lankatrails.lankatrails_backend.service.utils.TripPrivilegeUtils;

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
    TouristGuideRepository touristGuideRepository;

    @Autowired
    TripParticipantRepository tripParticipantRepository;

    @Autowired
    TripItemRepository tripItemRepository;

    @Autowired
    TripPrivilegeUtils tripPrivilegeUtils;

    @Autowired
    AuthUtils authUtils;

    @Autowired
    private AvailabilityService availabilityService;

    @Override
    @Transactional
    public APIResponse<AvailabilityResponse> checkAvailability(AvailabilityDto availabilityDto) {
        // Delegate to the availability service
        return availabilityService.checkAvailability(availabilityDto);
    }

    @Override
    @Transactional
    public APIResponse<String> addNewBooking(Long tripItemId) {
        TripParticipant tripParticipant = tripParticipantRepository.findByTourist_UserId(authUtils.loggedInUserId())
                .orElseThrow(() -> new RuntimeException("Trip Participant not found"));

        // Validate participant's privilege to book for the trip
        if (!tripPrivilegeUtils.hasPrivilege(tripParticipant.getTripRole(), TripPrivilege.ADD_BOOKINGS)) {
            throw new BadRequestException("Insufficient privileges to add bookings to this trip");
        }

        TripItem tripItem = tripItemRepository.findById(tripItemId)
                .orElseThrow(() -> new RuntimeException("Trip Item not found"));

        // Use pessimistic locking to prevent race conditions during booking
        Service serviceWithLock = serviceRepository.findByIdWithLock(tripItem.getService().getServiceId())
                .orElseThrow(() -> new RuntimeException("Service not found"));

        AvailabilityDto availabilityDto = AvailabilityDto.builder()
                .startDateTime(tripItem.getStartTime())
                .endDateTime(tripItem.getEndTime())
                .adultCount(tripItem.getTrip().getNumberOfAdults())
                .childCount(tripItem.getTrip().getNumberOfChildren())
                .serviceId(serviceWithLock.getServiceId())
                .tripId(tripItem.getTrip().getTripId())
                .noOfUnits(Optional.ofNullable(tripItem.getNoOfUnits()).orElse(1))
                .build();

        // Use the availability service to check availability
        APIResponse<String> availabilityResponse = availabilityService.validateServiceAvailability(availabilityDto);

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
                ()->new RuntimeException("Tour Guide not found")
        );
        //Get the current date
        LocalDate currentDate = LocalDate.now();
        //Get the day of the week which the current day belongs
        List<AvailableTime> availableTimeList = availabilitySlotRepository.findByService_ServiceId(id);
        if (availableTimeList.isEmpty()){
            throw new BadRequestException("No Availability Slots Defined");
        }
        DayOfWeek requestedStartDay = currentDate.getDayOfWeek();
        DayOfWeek requestedEndDay = currentDate.getDayOfWeek();
        Optional<AvailableTime> startDaySlot = availableTimeList.stream()
                .filter(slot -> slot.getDayOfWeek().equalsIgnoreCase(requestedStartDay.toString()))
                .findFirst();
        Optional<AvailableTime> endDaySlot = availableTimeList.stream()
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
            throw new RuntimeException("Hours per slot duration should be greater than 0");
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
        APIResponse<AvailabilityResponse> availabilityCheck = checkAvailability(availabilityDto);
        //if available, load all the free slots on the particular day it is selected
        if (availabilityCheck.isSuccess() && availabilityCheck.getData().isAvailable()){
            //Find the service
            Service service = serviceRepository.findById(id)
                    .orElseThrow(()-> new RuntimeException("Service not found"));
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
                .orElseThrow(()-> new RuntimeException("Service not found"));
        //Get the current date
        LocalDate currentDate = LocalDate.now();
        //Get the day of the week which the current day belongs
        List<AvailableTime> availableTimeList = availabilitySlotRepository.findByService_ServiceId(id);
        if (availableTimeList.isEmpty()){
            throw new RuntimeException("No Availability Slots Defined");
        }
        DayOfWeek requestedStartDay = currentDate.getDayOfWeek();
        DayOfWeek requestedEndDay = currentDate.getDayOfWeek();
        Optional<AvailableTime> startDaySlot = availableTimeList.stream()
                .filter(slot -> slot.getDayOfWeek().equalsIgnoreCase(requestedStartDay.toString()))
                .findFirst();
        Optional<AvailableTime> endDaySlot = availableTimeList.stream()
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

    private APIResponse<String> createSuccessResponse(String message) {
        return APIResponse.<String>builder()
                .success(true)
                .message(message)
                .data("")
                .build();
    }
}
