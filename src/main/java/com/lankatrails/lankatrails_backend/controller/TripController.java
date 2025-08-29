package com.lankatrails.lankatrails_backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lankatrails.lankatrails_backend.dtos.TripPeriodDto;
import com.lankatrails.lankatrails_backend.dtos.request.TripItemDTO;
import com.lankatrails.lankatrails_backend.dtos.request.TripRequestDTO;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.TripResponseDTO;
import com.lankatrails.lankatrails_backend.service.TripItemService;
import com.lankatrails.lankatrails_backend.service.TripService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/trips")
public class TripController {

    @Autowired
    private TripService tripService;

    @Autowired
    private TripItemService tripItemService;

    @PostMapping("/create")
    public ResponseEntity<APIResponse<TripResponseDTO>> createTrip(@Valid @RequestBody TripRequestDTO tripRequest) {
        log.info("Received request to create trip: {}", tripRequest);
        APIResponse<TripResponseDTO> response = tripService.createTrip(tripRequest);
        log.info("Trip created successfully: {}", response.getData());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/my-trips")
    public ResponseEntity<APIResponse<List<TripResponseDTO>>> getAllMyTrips() {
        log.info("Fetching all trips for the authenticated user");
        APIResponse<List<TripResponseDTO>> response = tripService.getAllMyTrips();
        log.info("Fetched {} trips", response.getData().size());
        return ResponseEntity.status(HttpStatus.OK)
                .body(response);
    }

    @GetMapping("/my-trip-period")
    public ResponseEntity<APIResponse<List<TripPeriodDto>>> getMyTripPeriod() {
        log.info("Fetching trip periods for the authenticated user");
        APIResponse<List<TripPeriodDto>> response = tripService.getMyTripPeriod();
        log.info("Fetched {} trip periods", response.getData().size());
        return ResponseEntity.status(HttpStatus.OK)
                .body(response);
    }

    @GetMapping("/{tripId}")
    public ResponseEntity<APIResponse<TripResponseDTO>> getTripById(@PathVariable Long tripId) {
        log.info("Fetching trip with ID: {}", tripId);
        APIResponse<TripResponseDTO> response = tripService.getTripById(tripId);
        log.info("Fetched trip: {}", response.getData());
        return ResponseEntity.status(HttpStatus.OK)
                .body(response);
    }

    @PostMapping("/add-trip-item/{tripId}")
    public ResponseEntity<APIResponse<String>> addTripItem(@PathVariable Long tripId, @Valid @RequestBody TripItemDTO tripItemDTO) {
        log.info("Adding trip item to trip with ID: {}", tripId);
        APIResponse<String> response = tripItemService.addTripItem(tripId, tripItemDTO);
        log.info("Trip item added successfully: {}", response.getData());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{tripId}/items")
    public ResponseEntity<APIResponse<List<TripItemDTO>>> getTripItemsByTripId(@PathVariable Long tripId) {
        log.info("Fetching trip items for trip with ID: {}", tripId);
        APIResponse<List<TripItemDTO>> response = tripService.getTripItemsByTripId(tripId);
        log.info("Fetched {} trip items", response.getData().size());
        return ResponseEntity.status(HttpStatus.OK)
                .body(response);
    }

    @PutMapping("/{tripId}/remove-tourist/{touristId}")
    public ResponseEntity<APIResponse<TripResponseDTO>> removeTouristFromTrip(@PathVariable Long tripId, @PathVariable Long touristId) {
        log.info("Removing tourist with ID: {} from trip with ID: {}", touristId, tripId);
        APIResponse<TripResponseDTO> response = tripService.removeTouristFromTrip(tripId, touristId);
        log.info("Tourist removed from trip successfully: {}", response.getData());
        return ResponseEntity.status(HttpStatus.OK)
                .body(response);
    }

}
