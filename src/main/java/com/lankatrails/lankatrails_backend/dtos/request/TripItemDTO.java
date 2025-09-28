package com.lankatrails.lankatrails_backend.dtos.request;

import com.lankatrails.lankatrails_backend.model.enums.TripItemType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class TripItemDTO {
    private TripItemType type;
    private PlaceDTO place;
    private ServiceDTO service;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer noOfUnits;
    private Integer numberOfAdults;
    private Integer numberOfChildren;
}
