package com.lankatrails.lankatrails_backend.service;

import com.lankatrails.lankatrails_backend.dtos.request.RateAndReviewDTO;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;

import java.util.List;

public interface ReviewService {
    APIResponse<RateAndReviewDTO> createReview(RateAndReviewDTO reviewRequest);
//    APIResponse<List<RateAndReviewDTO>> getReviewsByServiceId(Long serviceId);
//    APIResponse<RateAndReviewDTO> getReviewById(Long reviewId);
//    APIResponse<RateAndReviewDTO> updateReview(Long reviewId, RateAndReviewDTO reviewRequest, Long touristId);
//    APIResponse<String> deleteReview(Long reviewId, Long touristId);
//    APIResponse<Double> getAverageRatingByServiceId(Long serviceId);
//    APIResponse<List<RateAndReviewDTO>> getReviewsByTouristId(Long touristId);
}