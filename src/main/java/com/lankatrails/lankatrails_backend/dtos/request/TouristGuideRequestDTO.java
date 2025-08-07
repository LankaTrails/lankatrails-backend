package com.lankatrails.lankatrails_backend.dtos.request;

import java.util.List;

import com.lankatrails.lankatrails_backend.model.enums.TourGuideType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TouristGuideRequestDTO extends ServiceRequest{
    private List<String> languages;
    private TourGuideType tourGuideType;
}
