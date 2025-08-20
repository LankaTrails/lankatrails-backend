package com.lankatrails.lankatrails_backend.dtos;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class RateAndReviewDto {
    private Long id;
    private LocalDateTime createdDate;
    private Long rate;
    private String review;




}
