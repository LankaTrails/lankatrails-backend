package com.lankatrails.lankatrails_backend.service;

import com.lankatrails.lankatrails_backend.dtos.request.FavouriteItemDTO;
import com.lankatrails.lankatrails_backend.dtos.request.TripItemDTO;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.TouristProfileDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

public interface TouristService {
    APIResponse<TouristProfileDto> updateUserProfile(TouristProfileDto touristProfileDto, MultipartFile profilePic);

    APIResponse<FavouriteItemDTO> addFavourites(FavouriteItemDTO favouriteItemDTO);

    APIResponse<Set<FavouriteItemDTO>> getFavourites();
}
