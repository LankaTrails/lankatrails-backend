package com.lankatrails.lankatrails_backend.dtos.request;

import com.lankatrails.lankatrails_backend.model.enums.ServiceCategory;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProviderDetailsRequest {
    private Long ProviderId;

    private Double lat;
    private Double lng;
    private Double radiusKm;

    private String city;

    private ServiceCategory category;
}
