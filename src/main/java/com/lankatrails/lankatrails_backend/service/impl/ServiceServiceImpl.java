package com.lankatrails.lankatrails_backend.service.impl;

import com.lankatrails.lankatrails_backend.dtos.request.LocationDTO;
import com.lankatrails.lankatrails_backend.dtos.request.ProviderDetailsRequest;
import com.lankatrails.lankatrails_backend.dtos.request.ServiceDTO;
import com.lankatrails.lankatrails_backend.dtos.request.ServiceSearchRequestDTO;
import com.lankatrails.lankatrails_backend.dtos.response.*;
import com.lankatrails.lankatrails_backend.exception.BadRequestException;
import com.lankatrails.lankatrails_backend.exception.ResourceNotFoundException;
import com.lankatrails.lankatrails_backend.model.*;
import com.lankatrails.lankatrails_backend.model.enums.ServiceCategory;
import com.lankatrails.lankatrails_backend.model.enums.UploadCategory;
import com.lankatrails.lankatrails_backend.repositories.CategoryRepository;
import com.lankatrails.lankatrails_backend.repositories.ImageRepository;
import com.lankatrails.lankatrails_backend.repositories.ProviderRepository;
import com.lankatrails.lankatrails_backend.repositories.ServiceRepository;
import com.lankatrails.lankatrails_backend.service.ServiceService;
import com.lankatrails.lankatrails_backend.service.utils.FileUploadService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@org.springframework.stereotype.Service
public class ServiceServiceImpl implements ServiceService {
    @Autowired
    ServiceRepository serviceRepository;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    ImageRepository imageRepository;

    @Autowired
    ProviderRepository providerRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    FileUploadService fileUploadService;

//    @Transactional
//    @Override
//    public APIResponse<List<ServiceDTO>> searchServices(Double lat, Double lng, Double radiusKm, String city, String district, String province, String country) {
//        List<Service> results;
//
//        if (lat != null && lng != null && radiusKm != null) {
//            double radiusMeters = radiusKm * 1000;
//
//            // Nearby with optional metadata filter
//            results = serviceRepository.findNearbyServicesWithLocationFilter(
//                    lat, lng, radiusMeters, city, district, province, country
//            );
////            results = serviceRepository.findNearbyServices(lat, lng, radiusMeters);
//
//        } else if (city != null || district != null || province != null || country != null) {
//            // Text-only search
//            results = serviceRepository.findByLocationDetails(city, district, province, country);
//        } else {
//            // No filters given — optional: return all or empty
//            results = serviceRepository.findAll();
//        }
//
//        List<ServiceDTO> serviceDTOs = results.stream()
//                .map(service -> {
//                    ServiceDTO dto = new ServiceDTO();
//                    dto.setServiceId(service.getServiceId());
//                    dto.setServiceName(service.getServiceName());
//                    dto.setCategory(service.getCategory().getCategoryName());
////                    dto.setLocationBased(modelMapper.map(service.getLocationBased(), LocationDTO.class));
//                    dto.setMainImageUrl(service.getImages().getFirst().getImageUrl());
//                    return dto;
//                })
//                .toList();
//
//        return APIResponse.<List<ServiceDTO>>builder()
//                .success(true)
//                .message("Services found")
//                .data(serviceDTOs).build();
//    }

    @Override
    public APIResponse<String> addServiceImages(Long serviceId, MultipartFile[] serviceImages) {
        if (serviceImages == null || serviceImages.length == 0) {
            throw new BadRequestException("Service images cannot be null or empty", "ServiceImages", null);
        }

        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("serviceID", serviceId));

        Set<Image> images = new HashSet<>(fileUploadService.storeImages(serviceImages, UploadCategory.SERVICE_PICTURE));

        for (Image img : images) {
            img.setService(service);
            imageRepository.save(img);
        }

        return APIResponse.<String>builder()
                .success(true)
                .message("Successfully Added")
                .data("")
                .build();
    }

    @Override
    @Transactional
    public APIResponse<SearchResponseDTO> searchServicesAdvanced(ServiceSearchRequestDTO filter) {
        List<Service> services;

        // Step 1: Filter by location
        if (filter.getLat() != null && filter.getLng() != null && filter.getRadiusKm() != null) {
            services = serviceRepository.findNearbyServices(
                    filter.getLat(), filter.getLng(), filter.getRadiusKm() * 1000
            );
        } else if (filter.getCity() != null || filter.getDistrict() != null || filter.getProvince() != null || filter.getCountry() != null) {
            services = serviceRepository.findByLocationInSriLanka(filter.getCity());
        } else {
            services = serviceRepository.findAll();
        }

        // Step 2: Filter by category and subtype
        Stream<Service> filtered = services.stream();

        if (filter.getCategory() != null) {
            filtered = filtered.filter(s -> s.getCategory().getCategoryName().equals(filter.getCategory()));
        }

        if (filter.getAccommodationType() != null) {
            filtered = filtered.filter(s ->
                    s instanceof Accommodation acc &&
                            acc.getAccommodationCategory() != null &&
                            acc.getAccommodationCategory().getCategoryName().equals(filter.getAccommodationType())
            );
        }

        if (filter.getActivityType() != null) {
            filtered = filtered.filter(s ->
                    s instanceof ActivityService act &&
                            act.getActivityCategory() != null &&
                            act.getActivityCategory().getCategoryName().equals(filter.getActivityType())
            );
        }

        if (filter.getTourGuideType() != null) {
            filtered = filtered.filter(s ->
                    s instanceof TouristGuide guide &&
                            guide.getTourGuideCategory() != null &&
                            guide.getTourGuideCategory().getCategoryName().equals(filter.getTourGuideType())
            );
        }

        if (filter.getFoodAndBeverageType() != null) {
            filtered = filtered.filter(s ->
                    s instanceof FoodAndBeverage food &&
                            food.getFoodAndBeverageCategory() != null &&
                            food.getFoodAndBeverageCategory().getCategoryName().equals(filter.getFoodAndBeverageType())
            );
        }

        if (filter.getVehicleType() != null) {
            filtered = filtered.filter(s ->
                    s instanceof Transport vehicle &&
                            vehicle.getVehicleCategory() != null &&
                            vehicle.getVehicleCategory().getCategoryName().equals(filter.getVehicleType())
            );
        }

        // Step 3: Group by (provider + city + category)
        Map<String, List<Service>> groupedMap = filtered.collect(Collectors.groupingBy(service ->
                service.getProvider().getUserId() + "|" +
//                        service.getLocationBased().getCity() + "|" +
                        service.getCategory().getCategoryName()
        ));

        List<ProviderSearchDTO> groupedProviders = new ArrayList<>();
        List<ServiceSearchDTO> singleServices = new ArrayList<>();

        for (Map.Entry<String, List<Service>> entry : groupedMap.entrySet()) {
            List<Service> group = entry.getValue();

            if (group.size() == 1) {
                // Single service — map to ServiceSearchDTO
                Service service = group.getFirst();
                ServiceSearchDTO dto = new ServiceSearchDTO();
                dto.setServiceId(service.getServiceId());
                dto.setServiceName(service.getServiceName());
                dto.setCategory(service.getCategory().getCategoryName());
//                dto.setLocationBased(modelMapper.map(service.getLocationBased(), LocationDTO.class));
                dto.setPrice(service.getPrice());
                dto.setPriceType(service.getPriceType());
                dto.setMainImageUrl(service.getImages() != null && !service.getImages().isEmpty()
                        ? service.getImages().getFirst().getImageUrl()
                        : null);
                singleServices.add(dto);
            } else {
                // Grouped services — map to ProviderSearchDTO
                Service representative = group.getFirst();
                ProviderSearchDTO dto = new ProviderSearchDTO();
                dto.setProviderId(representative.getProvider().getUserId());
                dto.setBusinessName(representative.getProvider().getBusinessName());
//                dto.setLocation(modelMapper.map(representative.getLocationBased(), LocationDTO.class));
                dto.setCoverImageUrl(representative.getProvider().getCoverImageUrl());
                dto.setCategory(representative.getCategory().getCategoryName());
                groupedProviders.add(dto);
            }
        }

        SearchResponseDTO responseDTO = new SearchResponseDTO();
        responseDTO.setProviders(groupedProviders);
        responseDTO.setServices(singleServices);

        return APIResponse.<SearchResponseDTO>builder()
                .success(true)
                .message("Search results with grouped providers and individual services")
                .data(responseDTO)
                .build();
    }


    @Override
    @Transactional
    public APIResponse<ProviderDetailsDTO> getServicesByProviderAndCategory(Long providerId, ServiceCategory category) {
        Provider provider = providerRepository.findByUserId(providerId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider", providerId));

        Category categoryRequired = categoryRepository.findByCategoryName(category)
                .orElseThrow(() -> new ResourceNotFoundException("Category", String.valueOf(category)));

        List<Service> services = serviceRepository.findByProviderAndCategory(provider, categoryRequired);

        if (services.isEmpty()) {
            throw new ResourceNotFoundException("No services found for provider and category", providerId + " - " + category);
        }

        ProviderDetailsDTO providerDetails = new ProviderDetailsDTO();
        providerDetails.setProviderId(provider.getUserId());
        providerDetails.setBusinessName(provider.getBusinessName());
        providerDetails.setCoverImageUrl(provider.getCoverImageUrl());
        providerDetails.setCategory(categoryRequired.getCategoryName());
        providerDetails.setServices(services.stream()
                .map(service -> {
                    ServiceSearchDTO serviceSearchDTO = new ServiceSearchDTO();
                    serviceSearchDTO.setServiceId(service.getServiceId());
                    serviceSearchDTO.setServiceName(service.getServiceName());
                    serviceSearchDTO.setCategory(service.getCategory().getCategoryName());
                    serviceSearchDTO.setMainImageUrl(service.getImages() != null && !service.getImages().isEmpty()
                            ? service.getImages().getFirst().getImageUrl()
                            : null);
                    return serviceSearchDTO;
                })
                .collect(Collectors.toList()));
        return APIResponse.<ProviderDetailsDTO>builder()
                .success(true)
                .message("Services found for provider and category")
                .data(providerDetails)
                .build();
    }

    @Override
    @Transactional
    public APIResponse<ProviderDetailsDTO> getServicesByProviderAndCategory(ProviderDetailsRequest request) {
        Provider provider = providerRepository.findByUserId(request.getProviderId())
                .orElseThrow(() -> new ResourceNotFoundException("Provider", request.getProviderId()));

        Category categoryRequired = categoryRepository.findByCategoryName(request.getCategory())
                .orElseThrow(() -> new ResourceNotFoundException("Category", String.valueOf(request.getCategory())));

        List<Service> services;

        // Step 1: Filter by location
        if (request.getLat() != null && request.getLng() != null && request.getRadiusKm() != null) {
            services = serviceRepository.findNearbyServicesByProviderCategory(
                    request.getLat(), request.getLng(), request.getRadiusKm() * 1000, request.getProviderId(), Long.valueOf(categoryRequired.getCategoryId())
            );
        } else if (request.getCity() != null) {
            services = serviceRepository.findByLocationProviderCategory(request.getCity(), request.getProviderId(), Long.valueOf(categoryRequired.getCategoryId()));
        } else {
            services = serviceRepository.findByCategoryAndProvider(categoryRequired, provider);
        }

        if (services.isEmpty()) {
            throw new ResourceNotFoundException("No services found for provider and category", request.getProviderId() + " - " + request.getCategory());
        }

        ProviderDetailsDTO providerDetails = new ProviderDetailsDTO();
        providerDetails.setProviderId(provider.getUserId());
        providerDetails.setBusinessName(provider.getBusinessName());
        providerDetails.setBusinessDescription(provider.getBusinessDescription());
        providerDetails.setCoverImageUrl(provider.getCoverImageUrl());
        providerDetails.setLocation(modelMapper.map(provider.getLocation(), LocationDTO.class));
        providerDetails.setCategory(categoryRequired.getCategoryName());
        providerDetails.setServices(services.stream()
                .map(service -> {
                    ServiceSearchDTO serviceSearchDTO = new ServiceSearchDTO();
                    serviceSearchDTO.setServiceId(service.getServiceId());
                    serviceSearchDTO.setServiceName(service.getServiceName());
//                    serviceSearchDTO.setLocationBased(modelMapper.map(service.getLocationBased(), LocationDTO.class));
                    serviceSearchDTO.setPrice(service.getPrice());
                    serviceSearchDTO.setPriceType(service.getPriceType());
                    serviceSearchDTO.setCategory(service.getCategory().getCategoryName());
                    serviceSearchDTO.setMainImageUrl(service.getImages() != null && !service.getImages().isEmpty()
                            ? service.getImages().getFirst().getImageUrl()
                            : null);
                    return serviceSearchDTO;
                })
                .collect(Collectors.toList()));
        return APIResponse.<ProviderDetailsDTO>builder()
                .success(true)
                .message("Services found for provider and category")
                .data(providerDetails)
                .build();
    }


//    @Override
//    public APIResponse<ServiceRequest> removeAService(Long Id){
//        Service service=serviceRepository.findById(Id)
//                .orElseThrow(()->new ResourceNotFoundException("Activity Service",Id));
//        service.setStatus(false);
//        Service updatedService=serviceRepository.save(service);
//
//        ServiceRequest activityServiceResponse=new ServiceRequest();
//        activityServiceResponse.setServiceName(activityService.getServiceName());
//        activityServiceResponse.setServiceId(activityService.getServiceId());
//        activityServiceResponse.setStatus(activityService.getStatus());
//
//        return APIResponse.<ActivityServiceRequest>builder()
//                .success(true)
//                .message("Successfully Deleted")
//                .data(activityServiceResponse)
//                .build();
//
//    }
}
