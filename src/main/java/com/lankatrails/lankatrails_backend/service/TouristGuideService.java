package com.lankatrails.lankatrails_backend.service;

import com.lankatrails.lankatrails_backend.dtos.request.TouristGuideRequestDTO;
import com.lankatrails.lankatrails_backend.dtos.response.TouristGuideResponseDTO;

public interface TouristGuideService {
    TouristGuideResponseDTO getAllTourGuides();
    TouristGuideResponseDTO addNewTouristGuide(TouristGuideRequestDTO requestDTO);
    TouristGuideResponseDTO getGuideDetails(Long id);
    TouristGuideResponseDTO updateTourGuide(Long id);


}
