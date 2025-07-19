package com.lankatrails.lankatrails_backend.dtos.request;

import com.lankatrails.lankatrails_backend.model.enums.FoodAndBeverageType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FoodBeverageRequest extends ServiceRequest {
    private String openHours;
    private FoodAndBeverageType foodAndBeverageType;
    private Boolean vegetarianOptions;
    private Boolean halalCertified;
    private Boolean alcoholServed;
    private Boolean outdoorSeating;
    private Boolean liveMusic;
    private String cuisineType;
}
