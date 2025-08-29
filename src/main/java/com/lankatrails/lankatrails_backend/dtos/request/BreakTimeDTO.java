package com.lankatrails.lankatrails_backend.dtos.request;

import lombok.*;

import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BreakTimeDTO {
    private LocalTime breakStart;
    private LocalTime breakEnd;
}
