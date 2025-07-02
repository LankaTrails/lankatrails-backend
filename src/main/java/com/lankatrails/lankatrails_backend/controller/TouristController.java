package com.lankatrails.lankatrails_backend.controller;

import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.TouristProfileDto;
import com.lankatrails.lankatrails_backend.dtos.response.UserProfileDto;
import com.lankatrails.lankatrails_backend.service.TouristService;
import com.lankatrails.lankatrails_backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tourist")
@RequiredArgsConstructor
public class TouristController {

    private final TouristService touristService;

    @PutMapping("/update-profile")
    public ResponseEntity<APIResponse<TouristProfileDto>> updateProfile(
            @Valid @RequestBody TouristProfileDto touristProfileDto,
            HttpServletRequest request) {
        APIResponse<TouristProfileDto> updatedProfile = touristService.updateUserProfile(touristProfileDto, null);
        return ResponseEntity.status(HttpStatus.OK).body(updatedProfile);
    }
}
