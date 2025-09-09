package com.lankatrails.lankatrails_backend.dtos;


import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RateAndReviewDto {
    private Long id;
    private LocalDateTime createdDate;
    private Long rate;
    private String review;
    private TouristDto tourist;
}
