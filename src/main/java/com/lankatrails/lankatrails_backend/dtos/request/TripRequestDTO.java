package com.lankatrails.lankatrails_backend.dtos.request;

import com.lankatrails.lankatrails_backend.model.enums.TripStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Set;

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

    @NotNull(message = "Start location is required")
    private LocationDTO startLocation;

    @NotNull(message = "At least one location is required")
    private Set<LocationDTO> locations;

    private Integer numberOfAdults = 1;
    private Integer numberOfChildren = 0;

//    @NotNull(message = "Trip status is required")
    private TripStatus tripStatus;

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
