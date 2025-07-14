package com.lankatrails.lankatrails_backend.dtos.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LocationDTO {
    private String formattedAddress;
    private String city;
    private String district;
    private String province;
    private String country;
    private String postalCode;
    private Double latitude;
    private Double longitude;

}
