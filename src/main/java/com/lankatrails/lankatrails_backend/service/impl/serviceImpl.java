package com.lankatrails.lankatrails_backend.service.impl;

import com.lankatrails.lankatrails_backend.dtos.request.ServiceRequest;
import com.lankatrails.lankatrails_backend.exception.ResourceNotFoundException;
import com.lankatrails.lankatrails_backend.model.Location;
import com.lankatrails.lankatrails_backend.model.Service;
import com.lankatrails.lankatrails_backend.repositories.LocationRepository;
import com.lankatrails.lankatrails_backend.repositories.ServiceRepository;
import com.lankatrails.lankatrails_backend.service.ServicesForAll;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

@org.springframework.stereotype.Service
public class serviceImpl implements ServicesForAll {
    @Autowired
    ServiceRepository serviceRepository;

    @Autowired
    LocationRepository locationRepository;

    @Autowired
    ModelMapper modelMapper;

    public Boolean removeService(Long id){
        Service service=serviceRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Service",id));
        service.setStatus(false);
        return true;

    }

    public Location setServiceLocation(ServiceRequest request){
        if (request.getLocationId() != null) {
            // Fetch the location by ID
            return locationRepository.findLocationByLocationId(request.getLocationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Location", request.getLocationId()));
        } else {
            Location location = modelMapper.map(request.getLocationBased(), Location.class);
            return locationRepository.save(location);
        }
    }
}
