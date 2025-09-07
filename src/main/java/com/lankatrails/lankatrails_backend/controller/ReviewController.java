package com.lankatrails.lankatrails_backend.controller;

import com.lankatrails.lankatrails_backend.dtos.request.RateAndReviewRequest;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.RateAndReviewResponse;
import com.lankatrails.lankatrails_backend.security.utils.AuthUtils;
import com.lankatrails.lankatrails_backend.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private AuthUtils authUtils;

    // Create a new review
    @PostMapping("/{serviceId}")
    public ResponseEntity<APIResponse<String>> createReview(
            @PathVariable Long serviceId,
            @RequestBody RateAndReviewRequest reviewRequest) {
        APIResponse<String> response = reviewService.createReview(serviceId, reviewRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

   // Get all reviews for a specific service
    @GetMapping("/{serviceId}")
    public ResponseEntity<APIResponse<RateAndReviewResponse>> getReviewsByService(
            @PathVariable Long serviceId) {
        APIResponse<RateAndReviewResponse> response = reviewService.getReviewsByServiceId(serviceId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // Update a review (only by the author)
    @PutMapping("/{reviewId}")
    public ResponseEntity<APIResponse<String>> updateReview(
            @PathVariable Long reviewId,
            @RequestBody RateAndReviewRequest reviewRequest) {
        APIResponse<String> response = reviewService.updateReview(reviewId, reviewRequest);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // Delete a review (only by the author or admin)
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<APIResponse<String>> deleteReview(
            @PathVariable Long reviewId) {
        APIResponse<String> response = reviewService.deleteReview(reviewId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // Get average rating for a service
    @GetMapping("/{serviceId}/average")
    public ResponseEntity<APIResponse<RateAndReviewResponse>> getAverageRatingByService(
            @PathVariable Long serviceId) {
        APIResponse<RateAndReviewResponse> response = reviewService.getAverageRatingByServiceId(serviceId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}