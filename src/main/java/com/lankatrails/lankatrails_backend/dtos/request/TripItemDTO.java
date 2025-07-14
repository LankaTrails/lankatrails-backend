package com.lankatrails.lankatrails_backend.dtos.request;

import com.lankatrails.lankatrails_backend.model.enums.TripItemType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TripItemDTO {
    private TripItemType type;
    private PlaceDTO place;
    private ServiceDTO service;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
