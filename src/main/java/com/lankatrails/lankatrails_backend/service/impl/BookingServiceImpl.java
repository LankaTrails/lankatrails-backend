package com.lankatrails.lankatrails_backend.service.impl;

import com.lankatrails.lankatrails_backend.dtos.request.BookingRequestDTO;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.exception.BadCredentialsException;
import com.lankatrails.lankatrails_backend.exception.ResourceNotFoundException;
import com.lankatrails.lankatrails_backend.model.*;
import com.lankatrails.lankatrails_backend.model.enums.BookingType;
import com.lankatrails.lankatrails_backend.model.enums.ServiceCategory;
import com.lankatrails.lankatrails_backend.repositories.*;
import com.lankatrails.lankatrails_backend.security.utils.AuthUtils;
import com.lankatrails.lankatrails_backend.service.BookingService;
import jakarta.persistence.Table;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static com.lankatrails.lankatrails_backend.model.enums.BookingType.*;
import static com.lankatrails.lankatrails_backend.model.enums.ServiceCategory.ACCOMMODATION;

@Service
//@Slf4j
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
    AuthUtils authUtils;

    @Override
    @Transactional
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

            //check whether a booking is added on the same time slot
            List<Booking> sameSlot_touristBookings = bookingRepository.findByStartTimeAndEndTimeAndFromDateAndToDateAndTourist_UserId(
                    requestedStartTime,
                    requestedEndTime,
                    bookingRequestDTO.getFromDate(),
                    bookingRequestDTO.getToDate(),
                    authUtils.loggedInUser().getUserId()
            );
            BookingType bookingType = service.getBookingType();
            if (sameSlot_touristBookings.isEmpty()){
                    //check whether there are conflicting items in the trip when adding into bookings
                    List<Booking> overlapBookings = bookingRepository.findOverlappingBookings(
                            authUtils.loggedInUserId(),
                            bookingRequestDTO.getFromDate(),
                            bookingRequestDTO.getToDate(),
                            requestedStartTime,
                            requestedEndTime
                    );
                    //if no conflicting items are available for the booking
                    //validate for the availability of the quantities if available
                    if (overlapBookings.isEmpty()){
                        //start checking with the service
                        //trying to place the booking for multiple days
                        if (bookingType == MULTI_DAY){
                            //Find whether the service will get overlapping bookings because of the new booking
                            List<Booking> conflictingBookings=bookingRepository.findExactConflictingBookings(
                                    id,
                                    bookingRequestDTO.getFromDate(),
                                    requestedStartTime,
                                    bookingRequestDTO.getToDate(),
                                    requestedEndTime
                            );
                            //Get the total head count of the new booking received
                            Integer totalHeads = bookingRequestDTO.getChildCount() + bookingRequestDTO.getAdultCount();

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
                                    }
                                }else{
                                    return APIResponse.<String>builder()
                                            .success(true)
                                            .message("Available for Bookings")
                                            .data("")
                                            .build();
                                }

                            }


                        }
                        //trying to place the booking for a day
                        if (bookingType == ONE_DAY){


                        }

                        if (bookingType == TIME_SLOTS){

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
    public APIResponse<String> addNewBooking(BookingRequestDTO bookingRequestDTO, Long id) {
        Booking prepareBooking = new Booking();
        com.lankatrails.lankatrails_backend.model.Service service = serviceRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("Service",id));
        ServiceCategory category = service.getCategory().getCategoryName();

        switch (category){
            case ACCOMMODATION:

                break;
            case TRANSPORT:
                break;
            case ACTIVITY:
                break;
            case TOUR_GUIDE:
                break;
            case FOOD_BEVERAGE:
                break;

        }
//        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("HH:mm");
//        LocalTime time = LocalTime.parse(bookingRequestDTO.getToTime().toString(), inputFormatter);
        prepareBooking.setService(service);
        prepareBooking.setAdults(bookingRequestDTO.getAdultCount());
        prepareBooking.setChildren(bookingRequestDTO.getChildCount());
        prepareBooking.setEndTime(bookingRequestDTO.getToTime());
        prepareBooking.setStartTime(bookingRequestDTO.getFromTime());
        prepareBooking.setTourist((Tourist) authUtils.loggedInUser());
        prepareBooking.setBookingStatus(bookingRequestDTO.getBookingStatus());
        prepareBooking.setFromDate(bookingRequestDTO.getFromDate());
        prepareBooking.setToDate(bookingRequestDTO.getToDate());

        bookingRepository.save(prepareBooking);
        return null;
    }
}
