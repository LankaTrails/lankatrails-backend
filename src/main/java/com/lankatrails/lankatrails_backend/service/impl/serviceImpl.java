package com.lankatrails.lankatrails_backend.service.impl;

import com.lankatrails.lankatrails_backend.dtos.request.*;
import com.lankatrails.lankatrails_backend.exception.ResourceNotFoundException;
import com.lankatrails.lankatrails_backend.model.*;
import com.lankatrails.lankatrails_backend.model.enums.LocationType;
import com.lankatrails.lankatrails_backend.model.enums.ServiceStatus;
import com.lankatrails.lankatrails_backend.repositories.*;
import com.lankatrails.lankatrails_backend.service.ServicesForAll;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;


import java.util.*;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
@Slf4j
public class serviceImpl implements ServicesForAll {
    @Autowired
    ServiceRepository serviceRepository;

    @Autowired
    LocationRepository locationRepository;

    @Autowired
    AvailableTimeRepository availableTimeRepository;

    @Autowired
    BreakTimeRepository breakTimeRepository;

    @Autowired
    BookingConfigurationRepository bookingConfigurationRepository;

    @Autowired
    PriceConfigurationRepository priceConfigurationRepository;

    @Autowired
    ModelMapper modelMapper;

    public Boolean removeService(Long id){
        Service service=serviceRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Service",id));
        service.setStatus(ServiceStatus.INACTIVE);
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
                        location.setLocationType(LocationType.POINT_OF_INTEREST);
                        return locationRepository.save(location);
                    }
                }).collect(Collectors.toSet());
    }

    @Override
    public void setAvailableTime(List<AvailableTimeDTO> availableTimeDTOS, Service service) {
        log.debug("Availability Slots{}", availableTimeDTOS.toString());
        for (AvailableTimeDTO availableTime : availableTimeDTOS){
                if(availableTime.getOpenTime() != null && availableTime.getCloseTime() != null  || availableTime.getIs24Hours() != null || availableTime.getIsClosed() != null){
                    AvailableTime time = new AvailableTime();
                    time.setCloseTime(availableTime.getCloseTime());
                    time.setOpenTime(availableTime.getOpenTime());
                    time.setDayOfWeek(availableTime.getDayOfWeek());
                    time.setIs24Hours(availableTime.getIs24Hours());
                    time.setIsClosed(availableTime.getIsClosed());
                    time.setService(service);
                    time.setBreakTimes(new ArrayList<>());
                    AvailableTime savedAvailableTime = availableTimeRepository.save(time);

                    if(availableTime.getBreakTimes() != null){
                        for (BreakTimeDTO breakTimeDTO : availableTime.getBreakTimes()){
                            if(breakTimeDTO.getBreakStart() != null && breakTimeDTO.getBreakEnd() != null){
                                BreakTime breakTime = new BreakTime();
                                breakTime.setBreakStart(breakTimeDTO.getBreakStart());
                                breakTime.setBreakEnd(breakTimeDTO.getBreakEnd());
                                breakTime.setAvailableTime(time);
                                savedAvailableTime.getBreakTimes().add(breakTimeRepository.save(breakTime));
                            }
                        }
                    }
                }
        }
    }

    @Override
    @Transactional
    public BookingConfiguration setBookingConfig(BookingConfigDTO bookingConfigDTO){
        BookingConfiguration bookingConfiguration = modelMapper.map(bookingConfigDTO, BookingConfiguration.class);
        return bookingConfigurationRepository.save(bookingConfiguration);
    }

    @Override
    @Transactional
    public PriceConfiguration setPriceConfig(PriceConfigDTO priceConfigDTO){
        PriceConfiguration priceConfiguration = modelMapper.map(priceConfigDTO, PriceConfiguration.class);
        return priceConfigurationRepository.save(priceConfiguration);
    }

    @Override
    @Transactional
    public Optional<ServiceDTO> getServiceDto(Long serviceId) {
        return serviceRepository.findById(serviceId)
                .map(service -> new ServiceDTO(
                        service.getServiceId(),
                        service.getServiceName(),
                        service.getCategory().getCategoryName(),
                        service.getLocations().stream()
                                .map(location -> modelMapper.map(location, LocationDTO.class))
                                .collect(Collectors.toSet()),
                        service.getPriceConfiguration().getPriceWithType(),
                        service.getImages().getFirst().getImageUrl()
                ));
    }

    @Override
    public Map<Long, ServiceDTO> getServiceDtos(Set<Long> serviceIds) {
        return serviceRepository.findAllById(serviceIds)
                .stream()
                .collect(Collectors.toMap(
                        Service::getServiceId,
                        service -> new ServiceDTO(
                                service.getServiceId(),
                                service.getServiceName(),
                                service.getCategory().getCategoryName(),
                                service.getLocations().stream()
                                        .map(location -> modelMapper.map(location, LocationDTO.class))
                                        .collect(Collectors.toSet()),
                                service.getPriceConfiguration().getPriceWithType(),
                                service.getImages().isEmpty() ? null : service.getImages().getFirst().getImageUrl()
                        )
                ));
    }
}
