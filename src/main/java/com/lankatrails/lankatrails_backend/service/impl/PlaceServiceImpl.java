package com.lankatrails.lankatrails_backend.service.impl;

import com.lankatrails.lankatrails_backend.dtos.request.PlaceDTO;
import com.lankatrails.lankatrails_backend.exception.IllegalParamsException;
import com.lankatrails.lankatrails_backend.model.Place;
import com.lankatrails.lankatrails_backend.repositories.PlaceRepository;
import com.lankatrails.lankatrails_backend.service.PlaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class PlaceServiceImpl implements PlaceService {
    @Autowired
    private PlaceRepository placeRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public Place createPlace(PlaceDTO place) {
        if (place.getPlaceId() != null && placeRepository.existsById(place.getPlaceId())) {
            throw new IllegalParamsException("Place with this ID already exists");
        }
        Place newPlace = modelMapper.map(place, Place.class);
        log.info("Creating new place with ID: {}", newPlace.getPlaceId());
        return placeRepository.save(newPlace);
    }

    @Override
    public Place updatePlace(PlaceDTO place) {
        Place existingPlace = placeRepository.findById(place.getPlaceId())
                .orElseThrow(() -> new IllegalParamsException("Place not found"));
        existingPlace.setPlaceName(place.getPlaceName());
        existingPlace.setLatitude(place.getLatitude());
        existingPlace.setLongitude(place.getLongitude());
        existingPlace.setPhotoReference(place.getPhotoReference());
        existingPlace.setRating(place.getRating());
        log.info("Updating place with ID: {}", existingPlace.getPlaceId());
        return placeRepository.save(existingPlace);
    }
}
