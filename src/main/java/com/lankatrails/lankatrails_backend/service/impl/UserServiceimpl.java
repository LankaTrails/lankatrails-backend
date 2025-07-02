package com.lankatrails.lankatrails_backend.service.impl;

import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.ProviderProfileDto;
import com.lankatrails.lankatrails_backend.dtos.response.TouristProfileDto;
import com.lankatrails.lankatrails_backend.dtos.response.UserProfileDto;
import com.lankatrails.lankatrails_backend.exception.BadRequestException;
import com.lankatrails.lankatrails_backend.exception.ResourceNotFoundException;
import com.lankatrails.lankatrails_backend.exception.UnauthorizedException;
import com.lankatrails.lankatrails_backend.exception.UserNotFoundException;
import com.lankatrails.lankatrails_backend.model.Provider;
import com.lankatrails.lankatrails_backend.model.Tourist;
import com.lankatrails.lankatrails_backend.model.User;
import com.lankatrails.lankatrails_backend.model.enums.UserRole;
import com.lankatrails.lankatrails_backend.repositories.ProviderRepository;
import com.lankatrails.lankatrails_backend.repositories.TouristRepository;
import com.lankatrails.lankatrails_backend.repositories.UserRepository;
import com.lankatrails.lankatrails_backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceimpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TouristRepository touristRepository;

    @Autowired
    private ProviderRepository providerRepository;

    @Override
    public APIResponse<UserProfileDto> updateUserProfile(UserProfileDto userProfileDto, MultipartFile profilePic, HttpServletRequest request) {
        // check role and update accordingly
        switch (userProfileDto.getRole()) {
            case UserRole.ROLE_TOURIST:
                //cast to TouristProfileDto if needed
                TouristProfileDto touristProfileDto = (TouristProfileDto) userProfileDto;
                Tourist tourist = touristRepository.findById(userProfileDto.getId())
                        .orElseThrow(() -> new UserNotFoundException("Tourist not found with id: " + userProfileDto.getId()));
                tourist.setFirstName(touristProfileDto.getFirstName());
                tourist.setLastName(touristProfileDto.getLastName());
                tourist.setCountry(touristProfileDto.getCountry());

                //save the updated tourist profile
                touristRepository.save(tourist);

                return APIResponse.<UserProfileDto>builder()
                        .success(true)
                        .message("Tourist profile updated successfully")
                        .data(touristProfileDto)
                        .build();

            case UserRole.ROLE_PROVIDER:
                //cast to ProviderProfileDto if needed
                 ProviderProfileDto providerProfileDto = (ProviderProfileDto) userProfileDto;
                 Provider provider = providerRepository.findById(userProfileDto.getId())
                         .orElseThrow(() -> new UserNotFoundException("Provider not found with id: " + userProfileDto.getId()));

                    provider.setBusinessName(providerProfileDto.getBusinessName());
                    provider.setBusinessDescription(providerProfileDto.getBusinessDescription());

                    //save the updated provider profile
                    providerRepository.save(provider);
                    return APIResponse.<UserProfileDto>builder()
                            .success(true)
                            .message("Provider profile updated successfully")
                            .data(providerProfileDto)
                            .build();

            default:
                throw new UnauthorizedException( "Invalid user role: " + userProfileDto.getRole());
        }

    }
}
