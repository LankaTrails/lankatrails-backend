package com.lankatrails.lankatrails_backend.dtos;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class TripPeriodDto {
    private Long tripId;
    private String tripName;
    private LocalDate startDate;
    private LocalDate endDate;
}
