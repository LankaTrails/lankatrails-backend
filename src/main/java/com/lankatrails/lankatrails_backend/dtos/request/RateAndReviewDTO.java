package com.lankatrails.lankatrails_backend.dtos.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class RateAndReviewDTO {
    private Long reviewId;
    private LocalDateTime createdDate;
    private Long rate;
    private String review;

    private Long serviceId;           // keep service id simple
    private TouristSummaryDTO tourist; // who wrote the review

  }
