package com.lankatrails.lankatrails_backend.service;

import com.lankatrails.lankatrails_backend.dtos.request.*;
import com.lankatrails.lankatrails_backend.model.BookingConfiguration;
import com.lankatrails.lankatrails_backend.model.Location;
import com.lankatrails.lankatrails_backend.model.PriceConfiguration;
import com.lankatrails.lankatrails_backend.model.Service;
import com.lankatrails.lankatrails_backend.repositories.PriceConfigurationRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface ServicesForAll {
    Boolean removeService(Long id);

    Set<Location> setServiceLocation(ServiceRequest request);

    Optional<ServiceDTO> getServiceDto(Long serviceId);

    Map<Long, ServiceDTO> getServiceDtos(Set<Long> serviceIds);

    void setAvailableTime(List<AvailableTimeDTO> availabilitySlots, Service service);

    BookingConfiguration setBookingConfig(BookingConfigDTO bookingConfigDTO);

    PriceConfiguration setPriceConfig(PriceConfigDTO priceConfigDTO);

}
