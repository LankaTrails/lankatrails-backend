package com.lankatrails.lankatrails_backend.service;

import com.lankatrails.lankatrails_backend.dtos.request.TouristGuideRequestDTO;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.TouristGuideResponseDTO;
import com.lankatrails.lankatrails_backend.model.PolicySection;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface TouristGuideService {
    APIResponse<TouristGuideResponseDTO> getAllTourGuides(Integer pageNumber, Integer pageSize);
    TouristGuideResponseDTO addNewTouristGuide(TouristGuideRequestDTO requestDTO, List<MultipartFile> images);
    APIResponse<TouristGuideRequestDTO> searchWithId(Long id);
    TouristGuideResponseDTO updateTourGuide(Long id,TouristGuideRequestDTO requestDTO);
    APIResponse<String> addNewPolicy(PolicySection policies);
    APIResponse<String> updateService(Long id, TouristGuideRequestDTO requestDTO, List<MultipartFile> images );
    APIResponse<String> deleteService(Long Id);
}
