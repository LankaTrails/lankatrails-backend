package com.lankatrails.lankatrails_backend.service.impl;

import com.lankatrails.lankatrails_backend.dtos.RateAndReviewDto;
import com.lankatrails.lankatrails_backend.dtos.request.RateAndReviewRequest;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.RateAndReviewResponse;
import com.lankatrails.lankatrails_backend.exception.BadRequestException;
import com.lankatrails.lankatrails_backend.exception.UserNotFoundException;
import com.lankatrails.lankatrails_backend.model.RateAndReview;
import com.lankatrails.lankatrails_backend.model.Service;
import com.lankatrails.lankatrails_backend.model.Tourist;
import com.lankatrails.lankatrails_backend.repositories.ReviewRepository;
import com.lankatrails.lankatrails_backend.repositories.ServiceRepository;
import com.lankatrails.lankatrails_backend.repositories.TouristRepository;
import com.lankatrails.lankatrails_backend.security.utils.AuthUtils;
import com.lankatrails.lankatrails_backend.service.ReviewService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@org.springframework.stereotype.Service
@Slf4j
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private AuthUtils authUtils;

    @Autowired
    private TouristRepository touristRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public APIResponse<RateAndReviewRequest> createReview(Long serviceId, RateAndReviewRequest reviewRequest) {
        try {
            Tourist tourist = touristRepository.findByUserId(authUtils.loggedInUserId())
                    .orElseThrow(() -> new UserNotFoundException("User not found with id: " + authUtils.loggedInUserId()));

            Service service = serviceRepository.findById(serviceId)
                    .orElseThrow(() -> new BadRequestException("Service Not Found with id: " + serviceId));

            if (reviewRequest.getReview() == null ) {
                throw new BadRequestException("Review cannot be empty");
            }

            if (reviewRequest.getRate() == null) {
                throw  new BadRequestException("Rating is required");
            } else if (reviewRequest.getRate() < 1 || reviewRequest.getRate() > 5) {
                throw new BadRequestException("Rating must be between 1 and 5");
            }

            RateAndReview rateAndReview = modelMapper.map(reviewRequest, RateAndReview.class);
            rateAndReview.setService(service);
            rateAndReview.setTourist(tourist);
            RateAndReview savedReview = reviewRepository.save(rateAndReview);

            return APIResponse.<RateAndReviewRequest>builder()
                    .success(true)
                    .message("Review added successfully")
                    .data(modelMapper.map(savedReview, RateAndReviewRequest.class))
                    .build();

        } catch (Exception e) {
            log.error("Error creating review: {}", e.getMessage(), e);
            return APIResponse.<RateAndReviewRequest>builder()
                    .success(false)
                    .message("Failed to create review: " + e.getMessage())
                    .data(null)
                    .build();
        }
    }


    @Override
    public APIResponse<RateAndReviewResponse> getReviewsByServiceId(Long serviceId) {
        // Implementation to get reviews by service ID
        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new BadRequestException("Service Not Found with id: " + serviceId));
        List<RateAndReview> reviews = reviewRepository.findByService_ServiceId(serviceId);


        List<RateAndReviewDto> reviewDTOS = new ArrayList<>();
        for (RateAndReview review : reviews) {
            reviewDTOS.add(modelMapper.map(review, RateAndReviewDto.class));
        }

        APIResponse<RateAndReviewResponse> response = getAverageRatingByServiceId(serviceId);
        if (!response.isSuccess()) {
            return response;
        }

        RateAndReviewResponse rateAndReviewResponse = RateAndReviewResponse.builder()
                .averageRating(response.getData() != null ? response.getData().getAverageRating() : 0.0)
                .totalReviews((long) reviewDTOS.size())
                .reviews(reviewDTOS)
                .build();

        return APIResponse.<RateAndReviewResponse>builder()
                .success(true)
                .message("Reviews fetched successfully")
                .data(rateAndReviewResponse)
                .build();

    }

    @Override
    public APIResponse<RateAndReviewResponse> getAverageRatingByServiceId(Long serviceId) {
        try {
            serviceRepository.findById(serviceId)
                    .orElseThrow(() -> new BadRequestException("Service Not Found with id: " + serviceId));

            Long v = reviewRepository.countReviewsByServiceId(serviceId);   // number of reviews
            if (v == 0) {
                return APIResponse.<RateAndReviewResponse>builder()
                        .success(true)
                        .message("No reviews available for this service")
                        .data(null)
                        .build();
            }

            Double R = reviewRepository.findAverageRatingByServiceId(serviceId);  // service avg
            Double C = reviewRepository.findGlobalAverageRating();                // overall avg
            long m = 10L; // threshold

            double weightedRating = ( (double) v / (v + m) ) * R + ( (double) m / (v + m) ) * C;

            RateAndReviewResponse response = RateAndReviewResponse.builder()
                    .averageRating(weightedRating)
                    .totalReviews(v)
                    .reviews(null) // You can populate this with actual reviews if needed
                    .build();

            return APIResponse.<RateAndReviewResponse>builder()
                    .success(true)
                    .message("No reviews available for this service")
                    .data(null)
                    .build();

        } catch (Exception e) {
            log.error("Error calculating rating: {}", e.getMessage(), e);
            return APIResponse.<RateAndReviewResponse>builder()
                    .success(false)
                    .message("Failed to calculate rating: " + e.getMessage())
                    .data(null)
                    .build();
        }
    }
}
