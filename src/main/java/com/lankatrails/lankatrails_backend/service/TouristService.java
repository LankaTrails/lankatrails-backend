package com.lankatrails.lankatrails_backend.service;

import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.TouristProfileDto;
import org.springframework.web.multipart.MultipartFile;

public interface TouristService {
    APIResponse<TouristProfileDto> updateUserProfile(TouristProfileDto touristProfileDto, MultipartFile profilePic);
}
