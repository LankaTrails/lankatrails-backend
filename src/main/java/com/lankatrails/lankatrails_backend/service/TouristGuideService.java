package com.lankatrails.lankatrails_backend.service;

import com.lankatrails.lankatrails_backend.dtos.request.TouristGuideRequestDTO;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.TouristGuideResponseDTO;

public interface TouristGuideService {
    TouristGuideResponseDTO getAllTourGuides();
    TouristGuideResponseDTO addNewTouristGuide(TouristGuideRequestDTO requestDTO);
    APIResponse<TouristGuideRequestDTO> getGuideDetails(Long id);
    TouristGuideResponseDTO updateTourGuide(Long id,TouristGuideRequestDTO requestDTO);


}
