package com.lankatrails.lankatrails_backend.dtos;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityDto {
    private Integer childCount;
    private Integer adultCount;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private Long serviceId;
    private Long tripId;
}
