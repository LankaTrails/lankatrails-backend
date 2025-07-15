package com.lankatrails.lankatrails_backend.service.impl;

import com.lankatrails.lankatrails_backend.dtos.request.FavouriteItemDTO;
import com.lankatrails.lankatrails_backend.dtos.request.PlaceDTO;
import com.lankatrails.lankatrails_backend.dtos.request.ServiceDTO;
import com.lankatrails.lankatrails_backend.dtos.request.TripItemDTO;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.TouristProfileDto;
import com.lankatrails.lankatrails_backend.exception.IllegalParamsException;
import com.lankatrails.lankatrails_backend.exception.ResourceNotFoundException;
import com.lankatrails.lankatrails_backend.exception.UserNotFoundException;
import com.lankatrails.lankatrails_backend.model.Place;
import com.lankatrails.lankatrails_backend.model.Service;
import com.lankatrails.lankatrails_backend.model.Tourist;
import com.lankatrails.lankatrails_backend.model.enums.TripItemType;
import com.lankatrails.lankatrails_backend.repositories.PlaceRepository;
import com.lankatrails.lankatrails_backend.repositories.ServiceRepository;
import com.lankatrails.lankatrails_backend.repositories.TouristRepository;
import com.lankatrails.lankatrails_backend.security.utils.AuthUtils;
import com.lankatrails.lankatrails_backend.service.PlaceService;
import com.lankatrails.lankatrails_backend.service.TouristService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class TouristServiceImpl implements TouristService {
    @Autowired
    private TouristRepository touristRepository;

    @Autowired
    private PlaceRepository placeRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private PlaceService placeService;

    @Autowired
    private AuthUtils authUtils;

    @Autowired
    private ModelMapper modelMapper;

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

    @Override
    @Transactional
    public APIResponse<FavouriteItemDTO> addFavourites(FavouriteItemDTO favouriteItemDTO) {
        Tourist tourist = (Tourist) authUtils.loggedInUser();
        if (tourist == null) {
            log.error("Tourist not found for adding favourites");
            throw new UserNotFoundException("Tourist not found");
        }

        switch (favouriteItemDTO.getType()) {
            case PLACE:
                if (favouriteItemDTO.getPlace() == null) {
                    throw new IllegalParamsException("Place cannot be null");
                }
                if (placeRepository.findById(favouriteItemDTO.getPlace().getPlaceId()).isEmpty()) {
                    // If the place does not exist, add it to the repository
                    Place newPlace = placeService.createPlace(favouriteItemDTO.getPlace());
                    tourist.getFavouritePlaces().add(newPlace);
                    log.info("New place added to favourites: {} for User: {}", newPlace.getPlaceName(), tourist.getUserId());
                } else {
                    // If the place exists, update it and add to favourites
                    Place existingPlace = placeService.updatePlace(favouriteItemDTO.getPlace());
                    tourist.getFavouritePlaces().add(existingPlace);
                    log.info("Existing place updated and added to favourites: {} for User: {}", existingPlace.getPlaceName(), tourist.getUserId());
                }
                break;
            case SERVICE:
                if (favouriteItemDTO.getService() == null) {
                    throw new IllegalParamsException("Service cannot be null");
                }
                Service existingService = serviceRepository.findById(favouriteItemDTO.getService().getServiceId())
                        .orElseThrow(() -> new ResourceNotFoundException("ServiceID: " , favouriteItemDTO.getService().getServiceId()));
                tourist.getFavouriteServices().add(existingService);
                log.info("Service added to favourites: {} for User: {}", existingService.getServiceName(), tourist.getUserId());
                break;
            default:
                throw new IllegalParamsException("Invalid favourite item type");
        }
        touristRepository.save(tourist);

        return APIResponse.<FavouriteItemDTO>builder()
                .success(true)
                .message("Favourite item added successfully")
                .data(favouriteItemDTO)
                .build();
    }

    @Override
    @Transactional
    public APIResponse<Set<FavouriteItemDTO>> getFavourites() {
        Tourist tourist = (Tourist) authUtils.loggedInUser();
        if (tourist == null) {
            log.error("Tourist not found for fetching favourites");
            throw new UserNotFoundException("Tourist not found");
        }

        Set<FavouriteItemDTO> favouriteItems = Stream.concat(
                tourist.getFavouritePlaces().stream()
                        .map(place -> new FavouriteItemDTO(TripItemType.PLACE, modelMapper.map(place, PlaceDTO.class), null)),
                tourist.getFavouriteServices().stream()
                        .map(service -> new FavouriteItemDTO(TripItemType.SERVICE, null, modelMapper.map(service, ServiceDTO.class)))
        ).collect(Collectors.toSet());

        return APIResponse.<Set<FavouriteItemDTO>>builder()
                .success(true)
                .message("Favourite items fetched successfully")
                .data(favouriteItems)
                .build();
    }

}
