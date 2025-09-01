package com.lankatrails.lankatrails_backend.dtos.request;

import lombok.*;

import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvailableTimeDTO {
    private Long availableTimeId;
    private String dayOfWeek;
    private LocalTime openTime;
    private LocalTime closeTime;
    private Boolean is24Hours;
    private Boolean isClosed;
    private List<BreakTimeDTO> breakTimes;
}
