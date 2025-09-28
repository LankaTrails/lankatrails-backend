package com.lankatrails.lankatrails_backend.dtos.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class RateAndReviewRequest {
    private LocalDateTime createdDate;
    private Long rate;
    private String review;
}
