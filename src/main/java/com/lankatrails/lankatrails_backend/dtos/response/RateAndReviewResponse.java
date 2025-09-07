package com.lankatrails.lankatrails_backend.dtos.response;

import com.lankatrails.lankatrails_backend.dtos.RateAndReviewDto;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RateAndReviewResponse {
    private Double averageRating;
    private Long totalReviews;
    private List<RateAndReviewDto> reviews;
}
