package com.lankatrails.lankatrails_backend.service;

import com.lankatrails.lankatrails_backend.dtos.request.ServiceRequest;
import com.lankatrails.lankatrails_backend.dtos.response.TouristGuideResponseDTO;
import com.lankatrails.lankatrails_backend.model.Location;

public interface ServicesForAll {
    Boolean removeService(Long id);

    Location setServiceLocation(ServiceRequest request);

}
