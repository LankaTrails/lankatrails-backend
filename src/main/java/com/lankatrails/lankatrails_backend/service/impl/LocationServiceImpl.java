package com.lankatrails.lankatrails_backend.service.impl;

import com.lankatrails.lankatrails_backend.dtos.request.LocationDTO;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.model.Location;
import com.lankatrails.lankatrails_backend.repositories.LocationRepository;
import com.lankatrails.lankatrails_backend.service.LocationService;
import org.checkerframework.checker.units.qual.A;
import org.geolatte.geom.M;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LocationServiceImpl implements LocationService {
    @Autowired
    private LocationRepository locationRepository; // Assuming you have a repository for locations

    @Autowired
    private ModelMapper modelMapper; // Assuming you are using ModelMapper for DTO conversion

    @Override
    @Transactional
    public APIResponse<List<LocationDTO>> getAllCities() {
        List<Location> cities = locationRepository.findFirstLocationPerCity(); // This method should return a list of LocationDTOs
        if (cities.isEmpty()) {
            return APIResponse.<List<LocationDTO>>builder()
                    .success(false)
                    .message("No cities found")
                    .data(List.of())
                    .build();
        } else {
            List<LocationDTO> cityDTOs = cities.stream()
                    .map(location -> modelMapper.map(location, LocationDTO.class))
                    .toList(); // Convert each Location to LocationDTO
            return APIResponse.<List<LocationDTO>>builder()
                    .success(true)
                    .message("Cities retrieved successfully")
                    .data(cityDTOs)
                    .build();
        }
    }
}
