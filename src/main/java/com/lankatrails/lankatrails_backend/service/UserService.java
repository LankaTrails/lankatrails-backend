package com.lankatrails.lankatrails_backend.service;

import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.ProfilePicResponse;
import com.lankatrails.lankatrails_backend.dtos.response.UserProfileDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    APIResponse<UserProfileDto> updateUserProfile(UserProfileDto userProfileDto, MultipartFile profilePic, HttpServletRequest request);

    APIResponse<ProfilePicResponse> addProfilePicture(Long userId, MultipartFile profilePicture);
}
