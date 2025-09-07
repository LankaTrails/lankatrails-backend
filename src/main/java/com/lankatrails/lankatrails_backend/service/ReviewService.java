package com.lankatrails.lankatrails_backend.service;

import com.lankatrails.lankatrails_backend.dtos.request.RateAndReviewRequest;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.RateAndReviewResponse;

import java.util.List;

public interface ReviewService {
    APIResponse<String> createReview(Long serviceId, RateAndReviewRequest reviewRequest);
    APIResponse<RateAndReviewResponse> getReviewsByServiceId(Long serviceId);
    APIResponse<String > updateReview(Long reviewId, RateAndReviewRequest reviewRequest);
    APIResponse<String> deleteReview(Long reviewId);
    APIResponse<RateAndReviewResponse> getAverageRatingByServiceId(Long serviceId);
}