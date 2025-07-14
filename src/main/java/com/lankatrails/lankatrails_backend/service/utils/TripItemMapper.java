package com.lankatrails.lankatrails_backend.service.utils;

import com.lankatrails.lankatrails_backend.dtos.request.PlaceDTO;
import com.lankatrails.lankatrails_backend.dtos.request.ServiceDTO;
import com.lankatrails.lankatrails_backend.dtos.request.TripItemDTO;
import com.lankatrails.lankatrails_backend.model.Place;
import com.lankatrails.lankatrails_backend.model.TripItem;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TripItemMapper {
    @Autowired
    private ModelMapper modelMapper;

    public TripItemDTO toDTO(TripItem tripItem) {
        if (tripItem == null) {
            return null;
        }

        TripItemDTO dto = new TripItemDTO();
        dto.setType(tripItem.getTripItemType());
        dto.setStartTime(tripItem.getStartTime());
        dto.setEndTime(tripItem.getEndTime());
        switch (tripItem.getTripItemType()) {
            case PLACE:
                dto.setPlace(modelMapper.map(tripItem.getPlace(), PlaceDTO.class));
                dto.setService(null); // Ensure service is null for PLACE type
                return dto;
            case SERVICE:
                dto.setService(modelMapper.map(tripItem.getService(), ServiceDTO.class));
                dto.setPlace(null); // Ensure place is null for SERVICE type
                return dto;
            default:
                throw new IllegalArgumentException("Unknown TripItemType: " + tripItem.getTripItemType());
        }
    }

    public TripItem toEntity(TripItemDTO tripItemDTO) {
        if (tripItemDTO == null) {
            return null;
        }

        TripItem tripItem = new TripItem();
        tripItem.setTripItemType(tripItemDTO.getType());
        tripItem.setStartTime(tripItemDTO.getStartTime());
        tripItem.setEndTime(tripItemDTO.getEndTime());

        switch (tripItemDTO.getType()) {
            case PLACE:
                if (tripItemDTO.getPlace() != null) {
                    tripItem.setPlace(modelMapper.map(tripItemDTO.getPlace(), Place.class));
                }
                break;
            case SERVICE:
                if (tripItemDTO.getService() != null) {
                    tripItem.setService(modelMapper.map(tripItemDTO.getService(), com.lankatrails.lankatrails_backend.model.Service.class));
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown TripItemType: " + tripItemDTO.getType());
        }

        return tripItem;
    }
}
