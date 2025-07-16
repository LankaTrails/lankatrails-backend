package com.lankatrails.lankatrails_backend.service.impl;

import com.lankatrails.lankatrails_backend.dtos.request.LocationDTO;
import com.lankatrails.lankatrails_backend.dtos.request.ServiceDTO;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.exception.BadRequestException;
import com.lankatrails.lankatrails_backend.exception.ResourceNotFoundException;
import com.lankatrails.lankatrails_backend.model.Image;
import com.lankatrails.lankatrails_backend.model.Service;
import com.lankatrails.lankatrails_backend.model.enums.UploadCategory;
import com.lankatrails.lankatrails_backend.repositories.ImageRepository;
import com.lankatrails.lankatrails_backend.repositories.ServiceRepository;
import com.lankatrails.lankatrails_backend.service.ServiceService;
import com.lankatrails.lankatrails_backend.service.utils.FileUploadService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@org.springframework.stereotype.Service
public class ServiceServiceImpl implements ServiceService {
    @Autowired
    ServiceRepository serviceRepository;

    @Autowired
    ModelMapper modelMapper;

    @Transactional
    @Override
    public APIResponse<List<ServiceDTO>> searchServices(Double lat, Double lng, Double radiusKm, String city, String district, String province, String country) {
        List<Service> results;

        if (lat != null && lng != null && radiusKm != null) {
            double radiusMeters = radiusKm * 1000;

            // Nearby with optional metadata filter
            results = serviceRepository.findNearbyServicesWithLocationFilter(
                    lat, lng, radiusMeters, city, district, province, country
            );
//            results = serviceRepository.findNearbyServices(lat, lng, radiusMeters);

        } else if (city != null || district != null || province != null || country != null) {
            // Text-only search
            results = serviceRepository.findByLocationDetails(city, district, province, country);
        } else {
            // No filters given — optional: return all or empty
            results = serviceRepository.findAll();
        }

        List<ServiceDTO> serviceDTOs = results.stream()
                .map(service -> {
                    ServiceDTO dto = new ServiceDTO();
                    dto.setServiceId(service.getServiceId());
                    dto.setServiceName(service.getServiceName());
                    dto.setCategory(service.getCategory().getCategoryName());
                    dto.setLocationBased(modelMapper.map(service.getLocationBased(), LocationDTO.class));
                    dto.setMainImageUrl(service.getImages().getFirst().getImageUrl());
                    return dto;
                })
                .toList();

        return APIResponse.<List<ServiceDTO>>builder()
                .success(true)
                .message("Services found")
                .data(serviceDTOs).build();
    }
}
