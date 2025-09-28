package com.lankatrails.lankatrails_backend.controller;

import com.lankatrails.lankatrails_backend.dtos.request.FavouriteItemDTO;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.TouristProfileDto;
import com.lankatrails.lankatrails_backend.service.TouristService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

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

    @PostMapping("/add-favourites")
    public ResponseEntity<APIResponse<FavouriteItemDTO>> addFavourites(
            @Valid @RequestBody FavouriteItemDTO favouriteItemDTO) {
        APIResponse<FavouriteItemDTO> response = touristService.addFavourites(favouriteItemDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/favourites")
    public ResponseEntity<APIResponse<Set<FavouriteItemDTO>>> getFavourites() {
        APIResponse<Set<FavouriteItemDTO>> favourites = touristService.getFavourites();
        return ResponseEntity.status(HttpStatus.OK).body(favourites);
    }

    @DeleteMapping("/remove-favourite")
    public ResponseEntity<APIResponse<String>> removeFavourite(
            @Valid @RequestBody FavouriteItemDTO favouriteItemDTO) {
        APIResponse<String> response = touristService.removeFavourite(favouriteItemDTO);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
