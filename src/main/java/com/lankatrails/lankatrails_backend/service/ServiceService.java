package com.lankatrails.lankatrails_backend.service;

import com.lankatrails.lankatrails_backend.dtos.request.ProviderDetailsRequest;
import com.lankatrails.lankatrails_backend.dtos.request.ServiceSearchRequestDTO;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.ProviderDetailsDTO;
import com.lankatrails.lankatrails_backend.dtos.response.SearchResponseDTO;
import com.lankatrails.lankatrails_backend.model.enums.ServiceCategory;
import org.springframework.web.multipart.MultipartFile;

public interface ServiceService {
//    APIResponse<List<ServiceDTO>> searchServices(
//            Double lat,
//            Double lng,
//            Double radiusKm,
//            String city,
//            String district,
//            String province,
//            String country
//    );

    APIResponse<String> addServiceImages(Long serviceId, MultipartFile[] serviceImages);

    APIResponse<SearchResponseDTO> searchServicesAdvanced(ServiceSearchRequestDTO requestDTO);

    APIResponse<ProviderDetailsDTO> getServicesByProviderAndCategory(Long providerId, ServiceCategory category);

    APIResponse<ProviderDetailsDTO> getServicesByProviderAndCategory(ProviderDetailsRequest request);
}
