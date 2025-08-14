package com.lankatrails.lankatrails_backend.service;

import com.lankatrails.lankatrails_backend.dtos.request.ServiceDTO;
import com.lankatrails.lankatrails_backend.dtos.request.ServiceRequest;
import com.lankatrails.lankatrails_backend.dtos.response.TouristGuideResponseDTO;
import com.lankatrails.lankatrails_backend.model.Location;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface ServicesForAll {
    Boolean removeService(Long id);

    Set<Location> setServiceLocation(ServiceRequest request);

    Optional<ServiceDTO> getServiceDto(Long serviceId);

    Map<Long, ServiceDTO> getServiceDtos(Set<Long> serviceIds);

}
