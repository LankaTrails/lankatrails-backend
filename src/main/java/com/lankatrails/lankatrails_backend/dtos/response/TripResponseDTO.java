package com.lankatrails.lankatrails_backend.dtos.response;

import com.lankatrails.lankatrails_backend.dtos.request.LocationDTO;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
public class TripResponseDTO {
    private Long tripId;
    private String tripName;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocationDTO startLocation;
    private Set<LocationDTO> locations;
    private Integer numberOfAdults;
    private Integer numberOfChildren;
    private Double totalBudget;
    private Double totalBudgetLimit;
    private Double totalDistance;
}
