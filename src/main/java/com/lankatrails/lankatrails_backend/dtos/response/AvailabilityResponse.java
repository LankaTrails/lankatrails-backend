package com.lankatrails.lankatrails_backend.dtos.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AvailabilityResponse {
    private boolean available;
    private String message;
    private Integer availableUnits;
    private Integer requestedUnits;
    private Integer totalCapacity;
    private LocalDateTime suggestedAlternativeStart;
    private LocalDateTime suggestedAlternativeEnd;
}
