package com.lankatrails.lankatrails_backend.controller;

import com.lankatrails.lankatrails_backend.dtos.AvailabilityDto;
import com.lankatrails.lankatrails_backend.dtos.request.BookingRequestDTO;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.BookingResponseDTO;
import com.lankatrails.lankatrails_backend.dtos.response.TimeSlotsResponseDTO;
import com.lankatrails.lankatrails_backend.model.Booking;
import com.lankatrails.lankatrails_backend.service.impl.BookingServiceImpl;
import io.vavr.API;
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
    @GetMapping("/tourist/booking/check-timeslot")
    public ResponseEntity<APIResponse<String>> TimeSlotAvailability(@RequestBody AvailabilityDto availabilityDto){
        APIResponse<String> availabilityStatus =bookingService.checkAvailability(availabilityDto);
        return ResponseEntity.status(availabilityStatus.isSuccess()? HttpStatus.OK:HttpStatus.CONFLICT).body(availabilityStatus);
    }

    //insert new booking
    @PostMapping("/trip-item/{tripItemId}/book")
    public ResponseEntity<APIResponse<String>> createBooking(@PathVariable Long tripItemId){
        APIResponse<String>  createBooking = bookingService.addNewBooking(tripItemId);
        return ResponseEntity.status(createBooking.isSuccess()? HttpStatus.CREATED:HttpStatus.BAD_REQUEST).body(createBooking);
    }

    //get all bookings in a day for food-beverage
    @GetMapping("/provider/booking/food-beverage/{id}")
    public ResponseEntity<APIResponse<BookingResponseDTO>> getFoodBeverageBookings(@PathVariable  Long id, @RequestBody AvailabilityDto availabilityDto){
        APIResponse<BookingResponseDTO> loadBookings = bookingService.getBookingsOnTheDay(availabilityDto,id);
        return ResponseEntity.status(loadBookings.isSuccess()? HttpStatus.OK:HttpStatus.BAD_REQUEST).body(loadBookings);
    }

    //get all bookings in a day for transportation
    @GetMapping("/provider/booking/transport/{id}")
    public ResponseEntity<APIResponse<BookingResponseDTO>> getTransportBookings(@PathVariable  Long id, @RequestBody AvailabilityDto availabilityDto){
        APIResponse<BookingResponseDTO> loadBookings = bookingService.getBookingsOnTheDay(availabilityDto,id);
        return new ResponseEntity<>(loadBookings,HttpStatus.OK);
    }

    //get all bookings in a day for accommodation
    @GetMapping("/provider/booking/accommodation/{id}")
    public ResponseEntity<APIResponse<BookingResponseDTO>> getAccommodationBookings(@PathVariable  Long id, @RequestBody AvailabilityDto availabilityDto){
        APIResponse<BookingResponseDTO> loadBookings = bookingService.getBookingsOnTheDay(availabilityDto,id);
        return ResponseEntity.status(loadBookings.isSuccess()? HttpStatus.OK:HttpStatus.BAD_REQUEST).body(loadBookings);
    }

    //get all bookings in a day for tour-guide
    @GetMapping("/provider/booking/tour-guide/{id}")
    public ResponseEntity<APIResponse<BookingResponseDTO>> getTourGuideBookings(@PathVariable  Long id, @RequestBody AvailabilityDto availabilityDto){
        APIResponse<BookingResponseDTO> loadBookings = bookingService.getBookingsOnTheDay(availabilityDto,id);
        return ResponseEntity.status(loadBookings.isSuccess()? HttpStatus.OK:HttpStatus.BAD_REQUEST).body(loadBookings);
    }

    //get all bookings in a day for activity-provider
    @GetMapping("/provider/booking/activity-provider/{id}")
    public ResponseEntity<APIResponse<BookingResponseDTO>> getActivityBookings(@PathVariable  Long id, @RequestBody AvailabilityDto availabilityDto) {
        APIResponse<BookingResponseDTO> loadBookings = bookingService.getBookingsOnTheDay(availabilityDto,id);
        return ResponseEntity.status(loadBookings.isSuccess()? HttpStatus.OK:HttpStatus.BAD_REQUEST).body(loadBookings);
    }

    //Divide the tour-guide available time to slots based on the duration
    //Needed when placing the booking
    @GetMapping("/tourist/booking/tour-guide/slots/{id}")
    public ResponseEntity<APIResponse<List<String>>> getTouristDaySlots(@PathVariable Long id) {
        APIResponse<List<String>> getDaySlots = bookingService.getTourGuideDaySlots(id);
        return  ResponseEntity.status(getDaySlots.isSuccess()? HttpStatus.OK:HttpStatus.BAD_REQUEST).body(getDaySlots);

    }

    //Get the available free slots
    @GetMapping("/tourist/booking/available-slots/{id}")
    public ResponseEntity<APIResponse<List<String>>> getAvailableFreeSlots(@RequestBody AvailabilityDto availabilityDto, @PathVariable Long id){
        APIResponse<List<String>> response = bookingService.getAllFreeTimeSlots(availabilityDto,id);
        return ResponseEntity.status(response.isSuccess()? HttpStatus.OK:HttpStatus.BAD_REQUEST).body(response);
    }




}
