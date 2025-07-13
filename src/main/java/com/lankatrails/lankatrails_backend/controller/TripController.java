package com.lankatrails.lankatrails_backend.controller;

import com.lankatrails.lankatrails_backend.dtos.request.TripRequestDTO;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.TripResponseDTO;
import com.lankatrails.lankatrails_backend.service.TripService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/trips")
public class TripController {

    @Autowired
    private TripService tripService;

    @PostMapping("/create")
    public ResponseEntity<APIResponse<TripResponseDTO>> createTrip(@Valid @RequestBody TripRequestDTO tripRequest) {
        log.info("Received request to create trip: {}", tripRequest);
        APIResponse<TripResponseDTO> response = tripService.createTrip(tripRequest);
        log.info("Trip created successfully: {}", response.getData());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }
}
