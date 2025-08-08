package com.lankatrails.lankatrails_backend.controller;

import com.lankatrails.lankatrails_backend.dtos.request.BookingRequestDTO;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.service.impl.BookingServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tourist/booking")
public class BookingController {
    @Autowired
    BookingServiceImpl bookingService;
//    @GetMapping("/add-to-trip")
//    public ResponseEntity<APIResponse<Bo>>

    @GetMapping("/check-timeslot/{id}")
    public ResponseEntity<APIResponse<String>> TimeSlotAvailability(
            @RequestBody BookingRequestDTO bookingRequestDTO,
            @PathVariable Long id){
        APIResponse<String> availabilityStatus =bookingService.checkTimeSlotAvailability(bookingRequestDTO,id);
        return new ResponseEntity<>(availabilityStatus, HttpStatus.OK);
    }

    @PostMapping("/book/{id}")
    public ResponseEntity<APIResponse<String>> createBooking(
            @RequestBody BookingRequestDTO bookingRequestDTO,
            @PathVariable Long id
    ){
        APIResponse<String>  createBooking = bookingService.addNewBooking(bookingRequestDTO,id);
        return new ResponseEntity<>(createBooking,HttpStatus.OK);
    }




}
