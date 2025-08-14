package com.lankatrails.lankatrails_backend.controller;

import com.lankatrails.lankatrails_backend.dtos.request.BookingRequestDTO;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.BookingResponseDTO;
import com.lankatrails.lankatrails_backend.model.Booking;
import com.lankatrails.lankatrails_backend.service.impl.BookingServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class BookingController {
    @Autowired
    BookingServiceImpl bookingService;

    //check whether the time-slot is available for the booking
    @GetMapping("/tourist/booking/check-timeslot/{id}")
    public ResponseEntity<APIResponse<String>> TimeSlotAvailability(
            @RequestBody BookingRequestDTO bookingRequestDTO,
            @PathVariable Long id){
        APIResponse<String> availabilityStatus =bookingService.checkTimeSlotAvailability(bookingRequestDTO,id);
        return new ResponseEntity<>(availabilityStatus, HttpStatus.OK);
    }

    //insert new booking
    @PostMapping("/tourist/booking/book/{id}")
    public ResponseEntity<APIResponse<String>> createBooking(
            @RequestBody BookingRequestDTO bookingRequestDTO,
            @PathVariable Long id
    ){
        APIResponse<String>  createBooking = bookingService.addNewBooking(bookingRequestDTO,id);
        return new ResponseEntity<>(createBooking,HttpStatus.OK);
    }

    //get all bookings in a day for food-beverage
    @GetMapping("/booking/provider/food-beverage/{id}")
    public ResponseEntity<APIResponse<BookingResponseDTO>> getFoodBeverageBookings(
            @PathVariable  Long id,
            @RequestBody BookingRequestDTO bookingRequestDTO){
        APIResponse<BookingResponseDTO> loadBookings = bookingService.getBookingsOnTheDay(bookingRequestDTO,id);
        return new ResponseEntity<>(loadBookings,HttpStatus.OK);
    }

    //get all bookings in a day for transportation
    @GetMapping("/booking/provider/transport/{id}")
    public ResponseEntity<APIResponse<BookingResponseDTO>> getTransportBookings(
            @PathVariable  Long id,
            @RequestBody BookingRequestDTO bookingRequestDTO){
        APIResponse<BookingResponseDTO> loadBookings = bookingService.getBookingsOnTheDay(bookingRequestDTO,id);
        return new ResponseEntity<>(loadBookings,HttpStatus.OK);
    }
    //get all bookings in a day for accommodation
    @GetMapping("/booking/provider/accommodation/{id}")
    public ResponseEntity<APIResponse<BookingResponseDTO>> getAccommodationBookings(
            @PathVariable  Long id,
            @RequestBody BookingRequestDTO bookingRequestDTO){
        APIResponse<BookingResponseDTO> loadBookings = bookingService.getBookingsOnTheDay(bookingRequestDTO,id);
        return new ResponseEntity<>(loadBookings,HttpStatus.OK);
    }
    //get all bookings in a day for tour-guide
    @GetMapping("/booking/provider/tour-guide/{id}")
    public ResponseEntity<APIResponse<BookingResponseDTO>> getTourGuideBookings(
            @PathVariable  Long id,
            @RequestBody BookingRequestDTO bookingRequestDTO){
        APIResponse<BookingResponseDTO> loadBookings = bookingService.getBookingsOnTheDay(bookingRequestDTO,id);
        return new ResponseEntity<>(loadBookings,HttpStatus.OK);
    }

    //get all bookings in a day for activity-provider
    @GetMapping("/booking/provider/activity-provider/{id}")
    public ResponseEntity<APIResponse<BookingResponseDTO>> getActivityBookings(
            @PathVariable  Long id,
            @RequestBody BookingRequestDTO bookingRequestDTO){
        APIResponse<BookingResponseDTO> loadBookings = bookingService.getBookingsOnTheDay(bookingRequestDTO,id);
        return new ResponseEntity<>(loadBookings,HttpStatus.OK);
    }





}
