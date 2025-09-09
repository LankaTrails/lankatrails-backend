package com.lankatrails.lankatrails_backend.service.impl;

import com.lankatrails.lankatrails_backend.dtos.ProviderDto;
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
    @Transactional
    public void setAvailableTime(List<AvailableTimeDTO> availableTimeDTOS, Service service) {
        log.debug("Availability Slots {}", availableTimeDTOS);

        for (AvailableTimeDTO dto : availableTimeDTOS) {
            AvailableTime availableTime = dto.getAvailableTimeId() != null
                    ? availableTimeRepository.findById(dto.getAvailableTimeId()).orElse(null)
                    : null;

            if (availableTime == null) {
                // create new
                if ((dto.getOpenTime() != null && dto.getCloseTime() != null)
                        || Boolean.TRUE.equals(dto.getIs24Hours())
                        || Boolean.TRUE.equals(dto.getIsClosed())) {

                    availableTime = new AvailableTime();
                    availableTime.setService(service);
                }
            }

            if (availableTime != null) {
                // update common fields
                availableTime.setDayOfWeek(dto.getDayOfWeek());
                availableTime.setOpenTime(dto.getOpenTime());
                availableTime.setCloseTime(dto.getCloseTime());
                availableTime.setIs24Hours(dto.getIs24Hours());
                availableTime.setIsClosed(dto.getIsClosed());

                // handle break times
                availableTime.getBreakTimes().clear();
                if (dto.getBreakTimes() != null) {
                    for (BreakTimeDTO btDto : dto.getBreakTimes()) {
                        if (btDto.getBreakStart() != null && btDto.getBreakEnd() != null) {
                            BreakTime breakTime = new BreakTime();
                            breakTime.setBreakStart(btDto.getBreakStart());
                            breakTime.setBreakEnd(btDto.getBreakEnd());
                            breakTime.setAvailableTime(availableTime);
                            availableTime.getBreakTimes().add(breakTime);
                        }
                    }
                }

                availableTimeRepository.save(availableTime);
            }
        }
    }

    @Override
    @Transactional
    public BookingConfiguration setBookingConfig(BookingConfigDTO bookingConfigDTO){
        BookingConfiguration existingConfig = null;
        if (bookingConfigDTO.getBookingConfigId() != null) {
            existingConfig = bookingConfigurationRepository.findById(bookingConfigDTO.getBookingConfigId())
                    .orElse(null);
        }
        if (existingConfig != null) {
            modelMapper.map(bookingConfigDTO, existingConfig);
            return bookingConfigurationRepository.save(existingConfig);
        }
        BookingConfiguration bookingConfiguration = modelMapper.map(bookingConfigDTO, BookingConfiguration.class);
        return bookingConfigurationRepository.save(bookingConfiguration);
    }

    @Override
    @Transactional
    public PriceConfiguration setPriceConfig(PriceConfigDTO priceConfigDTO){
        PriceConfiguration existingConfig = null;
        if (priceConfigDTO.getPriceConfigId() != null) {
            existingConfig = priceConfigurationRepository.findById(priceConfigDTO.getPriceConfigId())
                    .orElse(null);
        }
        if (existingConfig != null) {
            modelMapper.map(priceConfigDTO, existingConfig);
            return priceConfigurationRepository.save(existingConfig);
        }
        PriceConfiguration priceConfiguration = modelMapper.map(priceConfigDTO, PriceConfiguration.class);
        return priceConfigurationRepository.save(priceConfiguration);
    }

    @Override
    @Transactional
    public Optional<ServiceDTO> getServiceDto(Long serviceId) {
        return serviceRepository.findById(serviceId)
                .map(service -> ServiceDTO.builder()
                        .serviceId(service.getServiceId())
                        .serviceName(service.getServiceName())
                        .Category(service.getCategory().getCategoryName())
                        .locations(service.getLocations().stream()
                                .map(location -> modelMapper.map(location, LocationDTO.class))
                                .collect(Collectors.toSet()))
                        .prices(service.getPriceConfiguration().getPriceWithType())
                        .mainImageUrl(service.getImages().isEmpty() ? null : service.getImages().getFirst().getImageUrl())
                        .provider(modelMapper.map(service.getProvider(), ProviderDto.class))
                        .build());
    }

    @Override
    public Map<Long, ServiceDTO> getServiceDtos(Set<Long> serviceIds) {
        return serviceRepository.findAllById(serviceIds)
                .stream()
                .collect(Collectors.toMap(
                        Service::getServiceId,
                        service -> ServiceDTO.builder()
                                .serviceId(service.getServiceId())
                                .serviceName(service.getServiceName())
                                .Category(service.getCategory().getCategoryName())
                                .locations(service.getLocations().stream()
                                        .map(location -> modelMapper.map(location, LocationDTO.class))
                                        .collect(Collectors.toSet()))
                                .prices(service.getPriceConfiguration().getPriceWithType())
                                .mainImageUrl(service.getImages().isEmpty() ? null : service.getImages().getFirst().getImageUrl())
                                .provider(modelMapper.map(service.getProvider(), ProviderDto.class))
                                .build()
                ));
    }
}
