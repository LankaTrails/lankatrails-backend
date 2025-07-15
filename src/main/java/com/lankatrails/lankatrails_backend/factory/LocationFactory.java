package com.lankatrails.lankatrails_backend.factory;

import com.lankatrails.lankatrails_backend.dtos.request.LocationDTO;
import com.lankatrails.lankatrails_backend.model.Location;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LocationFactory {

    public Location createFromDTO(LocationDTO locationDTO) {
        return Location.builder()
                .formattedAddress(locationDTO.getFormattedAddress())
                .city(locationDTO.getCity())
                .district(locationDTO.getDistrict())
                .province(locationDTO.getProvince())
                .country(locationDTO.getCountry())
                .postalCode(locationDTO.getPostalCode())
                .latitude(locationDTO.getLatitude())
                .longitude(locationDTO.getLongitude())
                .build();
    }
}
