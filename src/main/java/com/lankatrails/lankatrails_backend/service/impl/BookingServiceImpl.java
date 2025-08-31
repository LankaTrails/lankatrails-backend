package com.lankatrails.lankatrails_backend.service.impl;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.lankatrails.lankatrails_backend.model.*;
import com.lankatrails.lankatrails_backend.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.lankatrails.lankatrails_backend.dtos.AvailabilityDto;
import com.lankatrails.lankatrails_backend.dtos.request.BookingRequestDTO;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.AvailabilityResponse;
import com.lankatrails.lankatrails_backend.dtos.response.BookingResponseDTO;
import com.lankatrails.lankatrails_backend.exception.BadRequestException;
import com.lankatrails.lankatrails_backend.model.enums.BookingStatus;
import com.lankatrails.lankatrails_backend.model.enums.TripPrivilege;
import com.lankatrails.lankatrails_backend.security.utils.AuthUtils;
import com.lankatrails.lankatrails_backend.service.AvailabilityService;
import com.lankatrails.lankatrails_backend.service.BookingService;
import com.lankatrails.lankatrails_backend.service.utils.TripPrivilegeUtils;

import lombok.extern.slf4j.Slf4j;

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
            Double depositAmount = 0.0;
            if (Boolean.TRUE.equals(serviceWithLock.getPriceConfiguration().getRequiresDeposit())) {
                Double configDepositAmount = serviceWithLock.getPriceConfiguration().getDepositAmount();
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
                    .paidAmount(0.0)
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

    private APIResponse<String> createSuccessResponse(String message) {
        return APIResponse.<String>builder()
                .success(true)
                .message(message)
                .data("")
                .build();
    }
}
