package com.lankatrails.lankatrails_backend.controller;

import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.ProfilePicResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/service")
public class ServiceController {
    private final ;

    @PostMapping(value = "/{userId}/add-profile-picture", consumes = "multipart/form-data")
    public APIResponse<ProfilePicResponse> addProfilePicture(@PathVariable Long userId, MultipartFile profilePicture) {
        return userService.addProfilePicture(userId, profilePicture);
    }

}
