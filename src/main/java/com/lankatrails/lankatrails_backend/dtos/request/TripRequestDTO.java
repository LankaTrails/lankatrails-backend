package com.lankatrails.lankatrails_backend.dtos.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class TripRequestDTO {
    private Long tripId;

    @NotNull(message = "Trip name is required")
    private String tripName;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;
    private Integer numberOfPeople = 1;
    private Double totalBudget = 0.0;
    private Double totalBudgetLimit = 0.0;
    private Double totalDistance = 0.0;

    private Double accommodationLimit = 0.0;
    private Double foodLimit = 0.0;
    private Double transportLimit = 0.0;
    private Double activityLimit = 0.0;
    private Double shoppingLimit = 0.0;
    private Double miscellaneousLimit = 0.0;
}
