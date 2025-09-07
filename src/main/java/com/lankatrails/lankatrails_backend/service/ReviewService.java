package com.lankatrails.lankatrails_backend.service;

import com.lankatrails.lankatrails_backend.dtos.request.RateAndReviewRequest;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.RateAndReviewResponse;

import java.util.List;

public interface ReviewService {
    APIResponse<RateAndReviewRequest> createReview(Long serviceId, RateAndReviewRequest reviewRequest);
    APIResponse<RateAndReviewResponse> getReviewsByServiceId(Long serviceId);
//    APIResponse<RateAndReviewDTO> getReviewById(Long reviewId);
//    APIResponse<RateAndReviewDTO> updateReview(Long reviewId, RateAndReviewDTO reviewRequest, Long touristId);
//    APIResponse<String> deleteReview(Long reviewId, Long touristId);
    APIResponse<RateAndReviewResponse> getAverageRatingByServiceId(Long serviceId);
//    APIResponse<List<RateAndReviewDTO>> getReviewsByTouristId(Long touristId);
}