package com.lankatrails.lankatrails_backend.controller;

import com.lankatrails.lankatrails_backend.dtos.request.DeactivateAccountRequest;
import com.lankatrails.lankatrails_backend.dtos.UserPreferencesDTO;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.ProfilePicResponse;
import com.lankatrails.lankatrails_backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PreAuthorize("@ownership.isSelf(#userId)")
    @PostMapping(value = "/{userId}/add-profile-picture", consumes = "multipart/form-data")
    public APIResponse<ProfilePicResponse> addProfilePicture(@PathVariable Long userId, MultipartFile profilePicture) {
        return userService.addProfilePicture(userId, profilePicture);
    }

    /** The logged-in user disables their own account (password required). */
    @PostMapping("/deactivate")
    public ResponseEntity<APIResponse<String>> deactivateAccount(@Valid @RequestBody DeactivateAccountRequest request) {
        return ResponseEntity.ok(userService.deactivateAccount(request));
    }

    @PutMapping("/user-preferences")
    public ResponseEntity<APIResponse<UserPreferencesDTO>> updateUserPreferences(@Valid @RequestBody UserPreferencesDTO preference) {
        APIResponse<UserPreferencesDTO> response = userService.updateUserPreferences(preference);
        return ResponseEntity.status(HttpStatus.OK)
                .body(response);
    }
}
