package com.lankatrails.lankatrails_backend.dtos.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FoodBeverageRequest extends ServiceRequest {
    private String openHours;
}
