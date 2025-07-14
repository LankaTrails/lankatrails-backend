package com.lankatrails.lankatrails_backend.service.impl;

import com.lankatrails.lankatrails_backend.dtos.request.TripItemDTO;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.exception.IllegalParamsException;
import com.lankatrails.lankatrails_backend.exception.ResourceNotFoundException;
import com.lankatrails.lankatrails_backend.model.Place;
import com.lankatrails.lankatrails_backend.model.Trip;
import com.lankatrails.lankatrails_backend.model.TripItem;
import com.lankatrails.lankatrails_backend.model.enums.TripItemType;
import com.lankatrails.lankatrails_backend.repositories.PlaceRepository;
import com.lankatrails.lankatrails_backend.repositories.ServiceRepository;
import com.lankatrails.lankatrails_backend.repositories.TripItemRepository;
import com.lankatrails.lankatrails_backend.repositories.TripRepository;
import com.lankatrails.lankatrails_backend.service.TripItemService;
import com.lankatrails.lankatrails_backend.service.TripService;
import com.lankatrails.lankatrails_backend.service.utils.TripItemMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class TripItemServiceImpl implements TripItemService {
    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private TripItemRepository tripItemRepository;

    @Autowired
    private PlaceRepository placeRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private TripItemMapper tripItemMapper;

    @Override
    public APIResponse<TripItemDTO> addTripItem(Long tripId, TripItemDTO tripItemDTO) {
        log.info("Adding trip item to trip with ID: {}", tripId);

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("TripId", tripId));

        TripItem tripItem = modelMapper.map(tripItemDTO, TripItem.class);
        tripItem.setTrip(trip);

        switch (tripItemDTO.getType()) {
            case PLACE -> {
                if (tripItemDTO.getPlace() == null || tripItemDTO.getPlace().getPlaceId() == null) {
                    throw new IllegalParamsException("Place must be provided with a valid ID");
                }

                Place place;
                String placeId = tripItemDTO.getPlace().getPlaceId();

                // Check if place already exists
                place = placeRepository.findById(placeId).orElseGet(() -> {
                    // Save if not exist
                    return placeRepository.save(modelMapper.map(tripItemDTO.getPlace(), Place.class));
                });

                tripItem.setPlace(place);
            }

            case SERVICE -> {
                if (tripItemDTO.getService().getServiceId() == null) {
                    throw new IllegalParamsException("Service ID must be provided");
                }
                // Fetch the service by ID
                com.lankatrails.lankatrails_backend.model.Service service = serviceRepository.findById(tripItemDTO.getService().getServiceId())
                        .orElseThrow(() -> new IllegalParamsException("Service not found with ID: " + tripItemDTO.getService().getServiceId()));
                tripItem.setService(service);
            }

            default -> throw new IllegalParamsException("Invalid trip item type: " + tripItemDTO.getType());
        }

        TripItem savedTripItem = tripItemRepository.save(tripItem);
        TripItemDTO savedDTO = modelMapper.map(savedTripItem, TripItemDTO.class);

        return APIResponse.<TripItemDTO>builder()
                .success(true)
                .message("Trip item added successfully")
                .data(savedDTO)
                .build();
    }
}
