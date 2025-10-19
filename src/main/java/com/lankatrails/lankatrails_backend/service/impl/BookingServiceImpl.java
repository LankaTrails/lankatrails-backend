package com.lankatrails.lankatrails_backend.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.lankatrails.lankatrails_backend.dtos.BookingItemDto;
import com.lankatrails.lankatrails_backend.dtos.ProviderDto;
import com.lankatrails.lankatrails_backend.dtos.request.LocationDTO;
import com.lankatrails.lankatrails_backend.dtos.request.PaymentRequestDto;
import com.lankatrails.lankatrails_backend.dtos.request.ServiceDTO;
import com.lankatrails.lankatrails_backend.exception.ResourceNotFoundException;
import com.lankatrails.lankatrails_backend.model.*;
import com.lankatrails.lankatrails_backend.model.enums.TripItemType;
import com.lankatrails.lankatrails_backend.repositories.*;
import com.lankatrails.lankatrails_backend.service.PaymentService;
import com.stripe.model.PaymentIntent;
import org.modelmapper.ModelMapper;
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
                                .paymentAmount(BigDecimal.valueOf(paymentIntent.getAmount()).divide(BigDecimal.valueOf(100))) // Convert cents to dollars
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
    public APIResponse<BookingResponseDTO> getBookingsOnTheDay(AvailabilityDto availabilityDto,Long id){
        List<Booking> bookings = bookingRepository.findBookingsOnADay(availabilityDto.getStartDateTime().toLocalDate(),id,BookingStatus.CONFIRMED);
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

}
