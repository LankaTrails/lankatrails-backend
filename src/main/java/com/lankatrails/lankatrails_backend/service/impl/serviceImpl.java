package com.lankatrails.lankatrails_backend.service.impl;

import com.lankatrails.lankatrails_backend.dtos.request.AvailabilitySlotDTO;
import com.lankatrails.lankatrails_backend.dtos.request.ServiceRequest;
import com.lankatrails.lankatrails_backend.exception.ResourceNotFoundException;
import com.lankatrails.lankatrails_backend.model.ActivityService;
import com.lankatrails.lankatrails_backend.model.AvailabilitySlot;
import com.lankatrails.lankatrails_backend.model.Location;
import com.lankatrails.lankatrails_backend.model.Service;
import com.lankatrails.lankatrails_backend.repositories.AvailabilitySlotRepository;
import com.lankatrails.lankatrails_backend.repositories.LocationRepository;
import com.lankatrails.lankatrails_backend.repositories.ServiceRepository;
import com.lankatrails.lankatrails_backend.service.ServicesForAll;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;


import java.util.Set;
import java.util.stream.Collectors;
import java.util.List;

@org.springframework.stereotype.Service
@Slf4j
public class serviceImpl implements ServicesForAll {
    @Autowired
    ServiceRepository serviceRepository;

    @Autowired
    LocationRepository locationRepository;

    @Autowired
    AvailabilitySlotRepository availabilitySlotRepository;

    @Autowired
    ModelMapper modelMapper;

    public Boolean removeService(Long id){
        Service service=serviceRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Service",id));
        service.setStatus(false);
        return true;

    }

//    public Location setServiceLocation(ServiceRequest request){
//        if (request.getLocationId() != null) {
//            log.info("Fetching existing location with ID: {}", request.getLocationId());
//            // Fetch the location by ID
//            return locationRepository.findLocationByLocationId(request.getLocationId())
//                    .orElseThrow(() -> new ResourceNotFoundException("Location", request.getLocationId()));
//        } else {
//            log.info("Creating new location from request: {}", request.getLocationBased());
//            Location location = modelMapper.map(request.getLocationBased(), Location.class);
//            return locationRepository.save(location);
//        }
//    }

    public Set<Location> setServiceLocation(ServiceRequest request){
        return request.getLocations().stream()
                .map(locationDTO -> {
                    if (locationDTO.getLocationId() != null) {
                        log.info("Fetching existing location with ID: {}", locationDTO.getLocationId());
                        return locationRepository.findLocationByLocationId(locationDTO.getLocationId())
                                .orElseThrow(() -> new ResourceNotFoundException("Location", locationDTO.getLocationId()));
                    } else {
                        log.info("Creating new location from request: {}", locationDTO);
                        Location location = modelMapper.map(locationDTO, Location.class);
                        return locationRepository.save(location);
                    }
                }).collect(Collectors.toSet());
    }

    public void setAvailabilitySlots(List<AvailabilitySlotDTO> availabilitySlots, ActivityService activityService){
        log.debug("Availability Slots{}", availabilitySlots.toString());
        for (AvailabilitySlotDTO availabilitySlot : availabilitySlots){
            List<AvailabilitySlot> checkDb= availabilitySlotRepository.findByService_ServiceId(activityService.getServiceId());
            if (checkDb.isEmpty()){
//               AvailabilitySlot slot = modelMapper.map(availabilitySlot,AvailabilitySlot.class);
                AvailabilitySlot slot = new AvailabilitySlot();
                slot.setCloseTime(availabilitySlot.getCloseTime());
                slot.setOpenTime(availabilitySlot.getOpenTime());
                slot.setDayOfWeek(availabilitySlot.getDayOfWeek());
                slot.setService(activityService);
                availabilitySlotRepository.save(slot);
            }
        }

    }
}
