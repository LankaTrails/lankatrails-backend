package com.lankatrails.lankatrails_backend.service;

import com.lankatrails.lankatrails_backend.dtos.request.TouristGuideRequestDTO;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.TouristGuideResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface TouristGuideService {
    APIResponse<TouristGuideResponseDTO> getAllTourGuides(Integer pageNumber, Integer pageSize);
    TouristGuideResponseDTO addNewTouristGuide(TouristGuideRequestDTO requestDTO, List<MultipartFile> images);
    APIResponse<TouristGuideRequestDTO> getGuideDetails(Long id);
    TouristGuideResponseDTO updateTourGuide(Long id,TouristGuideRequestDTO requestDTO);


}
