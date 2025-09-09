package com.lankatrails.lankatrails_backend.dtos.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import com.lankatrails.lankatrails_backend.dtos.ChatRoomDto;
import com.lankatrails.lankatrails_backend.dtos.request.LocationDTO;

import lombok.Getter;
import lombok.Setter;

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
    private BigDecimal totalBudget;
    private BigDecimal totalBudgetLimit;
    private Double totalDistance;
    private ChatRoomDto chatRoom;
    private String tripStatus;
}
