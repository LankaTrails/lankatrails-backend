package com.lankatrails.lankatrails_backend.dtos.request;

import com.lankatrails.lankatrails_backend.model.enums.TripStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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
    private List<LocationDTO> locations;

    private Integer numberOfAdults = 1;
    private Integer numberOfChildren = 0;

    //    @NotNull(message = "Trip status is required")
    private TripStatus tripStatus;

    private BigDecimal totalBudget = BigDecimal.ZERO;
    private BigDecimal totalBudgetLimit = BigDecimal.ZERO;
    private Double totalDistance = 0.0;

    private BigDecimal accommodationLimit = BigDecimal.ZERO;
    private BigDecimal foodLimit = BigDecimal.ZERO;
    private BigDecimal transportLimit = BigDecimal.ZERO;
    private BigDecimal activityLimit = BigDecimal.ZERO;
    private BigDecimal shoppingLimit = BigDecimal.ZERO;
    private BigDecimal miscellaneousLimit = BigDecimal.ZERO;
}
