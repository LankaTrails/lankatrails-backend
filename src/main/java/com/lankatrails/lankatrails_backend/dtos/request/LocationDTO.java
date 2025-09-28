package com.lankatrails.lankatrails_backend.dtos.request;

import com.lankatrails.lankatrails_backend.model.enums.LocationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LocationDTO {
    private Long locationId;
    private String formattedAddress;
    private String city;
    private String district;
    private String province;
    private String country;
    private String postalCode;
    private Double latitude;
    private Double longitude;
    private LocationType locationType;

}
