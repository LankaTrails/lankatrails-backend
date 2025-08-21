package com.lankatrails.lankatrails_backend.service.impl;

import com.lankatrails.lankatrails_backend.dtos.request.RateAndReviewDTO;
import com.lankatrails.lankatrails_backend.dtos.request.TouristSummaryDTO;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
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
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    public APIResponse<RateAndReviewDTO> createReview(RateAndReviewDTO reviewRequest) {
        try {
            Tourist tourist = touristRepository.findByUserId(authUtils.loggedInUserId())
                    .orElseThrow(() -> new UserNotFoundException("User not found with id: " + authUtils.loggedInUserId()));

            Service service = serviceRepository.findById(reviewRequest.getServiceId())
                    .orElseThrow(() -> new BadRequestException("Service Not Found with id: " + reviewRequest.getServiceId()));

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

            return APIResponse.<RateAndReviewDTO>builder()
                    .success(true)
                    .message("Review added successfully")
                    .data(modelMapper.map(savedReview, RateAndReviewDTO.class))
                    .build();

        } catch (Exception e) {
            log.error("Error creating review: {}", e.getMessage(), e);
            return APIResponse.<RateAndReviewDTO>builder()
                    .success(false)
                    .message("Failed to create review: " + e.getMessage())
                    .data(null)
                    .build();
        }
    }


    @Override
    public APIResponse<List<RateAndReviewDTO>> getReviewsByServiceId(Long serviceId) {
        // Implementation to get reviews by service ID
        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new BadRequestException("Service Not Found with id: " + serviceId));
        List<RateAndReview> reviews = reviewRepository.findByService_ServiceId(serviceId);


        List<RateAndReviewDTO> reviewDTOS = new ArrayList<>();
        for (RateAndReview review : reviews) {
            reviewDTOS.add(modelMapper.map(review, RateAndReviewDTO.class));
        }
        return APIResponse.<List<RateAndReviewDTO>>builder()
                .success(true)
                .message("Reviews retrieved successfully")
                .data(reviewDTOS)
                .build();
    }
//
//    // Implement other methods similarly...
}
