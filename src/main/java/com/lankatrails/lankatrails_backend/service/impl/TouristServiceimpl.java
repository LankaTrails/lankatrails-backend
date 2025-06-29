package com.lankatrails.lankatrails_backend.service.impl;

import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.TouristProfileDto;
import com.lankatrails.lankatrails_backend.dtos.response.UserProfileDto;
import com.lankatrails.lankatrails_backend.exception.UserNotFoundException;
import com.lankatrails.lankatrails_backend.model.Tourist;
import com.lankatrails.lankatrails_backend.repositories.TouristRepository;
import com.lankatrails.lankatrails_backend.security.utils.AuthUtils;
import com.lankatrails.lankatrails_backend.service.TouristService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class TouristServiceimpl implements TouristService {
    @Autowired
    private TouristRepository touristRepository;

    @Autowired
    private AuthUtils authUtils;

    @Override
    public APIResponse<TouristProfileDto> updateUserProfile(TouristProfileDto touristProfileDto, MultipartFile profilePic) {
        Long touristId = authUtils.loggedInUserId();
        Tourist tourist = touristRepository.findById(touristId)
                .orElseThrow(() -> new UserNotFoundException("Tourist not found with id: " + touristProfileDto.getId()));
        tourist.setFirstName(touristProfileDto.getFirstName());
        tourist.setLastName(touristProfileDto.getLastName());
        tourist.setCountry(touristProfileDto.getCountry());

        //save the updated tourist profile
        Tourist updatedTourist = touristRepository.save(tourist);

        TouristProfileDto updatedProfile = TouristProfileDto.builder()
                .id(updatedTourist.getUserId())
                .firstName(updatedTourist.getFirstName())
                .lastName(updatedTourist.getLastName())
                .country(updatedTourist.getCountry())
                .build();

        return APIResponse.<TouristProfileDto>builder()
                .success(true)
                .message("Tourist profile updated successfully")
                .data(updatedProfile)
                .build();
    }
}
