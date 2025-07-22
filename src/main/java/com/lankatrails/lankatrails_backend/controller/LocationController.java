package com.lankatrails.lankatrails_backend.controller;

import com.lankatrails.lankatrails_backend.dtos.request.LocationDTO;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.service.LocationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/locations")
public class LocationController {

    @Autowired
    LocationService locationService;

    @GetMapping("/cities")
    public ResponseEntity<APIResponse<List<LocationDTO>>> getAllCities() {
        log.info("Fetching all cities");
        APIResponse<List<LocationDTO>> response = locationService.getAllCities();
        return ResponseEntity.ok(response);
    }
}
