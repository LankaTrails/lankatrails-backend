package com.lankatrails.lankatrails_backend.controller;

import com.lankatrails.lankatrails_backend.dtos.request.RateAndReviewDTO;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.security.utils.AuthUtils;
import com.lankatrails.lankatrails_backend.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tourist")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private AuthUtils authUtils;

    // Create a new review
    @PostMapping("/reviews")
    public ResponseEntity<APIResponse<RateAndReviewDTO>> createReview(
            @RequestBody RateAndReviewDTO reviewRequest) {
        // Set the tourist ID from the authenticated user
        Long touristId = authUtils.loggedInUserId();
        reviewRequest.getTourist().setId(touristId);

        APIResponse<RateAndReviewDTO> response = reviewService.createReview(reviewRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

//     Get all reviews for a specific service
    @GetMapping("/reviews/{serviceId}")
    public ResponseEntity<APIResponse<List<RateAndReviewDTO>>> getReviewsByService(
            @PathVariable Long serviceId) {
        APIResponse<List<RateAndReviewDTO>> response = reviewService.getReviewsByServiceId(serviceId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
//
//    // Get a specific review by ID
//    @GetMapping("/{reviewId}")
//    public ResponseEntity<APIResponse<RateAndReviewDTO>> getReviewById(
//            @PathVariable Long reviewId) {
//        APIResponse<RateAndReviewDTO> response = reviewService.getReviewById(reviewId);
//        return new ResponseEntity<>(response, HttpStatus.OK);
//    }
//
//    // Update a review (only by the author)
//    @PutMapping("/{reviewId}")
//    public ResponseEntity<APIResponse<RateAndReviewDTO>> updateReview(
//            @PathVariable Long reviewId,
//            @RequestBody RateAndReviewDTO reviewRequest) {
//        Long touristId = authUtils.loggedInUserId();
//        APIResponse<RateAndReviewDTO> response = reviewService.updateReview(reviewId, reviewRequest, touristId);
//        return new ResponseEntity<>(response, HttpStatus.OK);
//    }
//
//    // Delete a review (only by the author or admin)
//    @DeleteMapping("/{reviewId}")
//    public ResponseEntity<APIResponse<String>> deleteReview(
//            @PathVariable Long reviewId) {
//        Long touristId = authUtils.loggedInUserId();
//        APIResponse<String> response = reviewService.deleteReview(reviewId, touristId);
//        return new ResponseEntity<>(response, HttpStatus.OK);
//    }
//
//    // Get average rating for a service
//    @GetMapping("/service/{serviceId}/average-rating")
//    public ResponseEntity<APIResponse<Double>> getAverageRatingByService(
//            @PathVariable Long serviceId) {
//        APIResponse<Double> response = reviewService.getAverageRatingByServiceId(serviceId);
//        return new ResponseEntity<>(response, HttpStatus.OK);
//    }
//
//    // Get reviews by tourist (current user)
//    @GetMapping("/my-reviews")
//    public ResponseEntity<APIResponse<List<RateAndReviewDTO>>> getMyReviews() {
//        Long touristId = authUtils.loggedInUserId();
//        APIResponse<List<RateAndReviewDTO>> response = reviewService.getReviewsByTouristId(touristId);
//        return new ResponseEntity<>(response, HttpStatus.OK);
//    }
}