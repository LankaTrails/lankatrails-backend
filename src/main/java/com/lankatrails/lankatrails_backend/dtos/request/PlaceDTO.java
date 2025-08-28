package com.lankatrails.lankatrails_backend.dtos.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PlaceDTO {
    private String placeId;
    private String placeName;
    private String photoReference;
    private Double latitude;
    private Double longitude;
    private Double rating;
}
