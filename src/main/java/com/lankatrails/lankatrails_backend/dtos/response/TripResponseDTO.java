package com.lankatrails.lankatrails_backend.dtos.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class TripResponseDTO {
    private Long tripId;
    private String tripName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer numberOfPeople;
    private Double totalBudget;
    private Double totalBudgetLimit;
    private Double totalDistance;
}
