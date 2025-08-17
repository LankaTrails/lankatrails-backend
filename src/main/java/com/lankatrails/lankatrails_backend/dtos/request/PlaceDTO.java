package com.lankatrails.lankatrails_backend.dtos.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlaceDTO {
    private String placeId;
    private String placeName;
    private String photoReference;
    private Double latitude;
    private Double longitude;
    private Double rating;
}
