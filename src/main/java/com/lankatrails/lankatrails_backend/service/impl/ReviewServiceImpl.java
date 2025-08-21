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

    private List<String> validateReviewRequest(RateAndReviewDTO reviewRequest) {
        List<String> errors = new ArrayList<>();

        if (reviewRequest.getRate() == null) {
            errors.add("Rating is required");
        } else if (reviewRequest.getRate() < 1 || reviewRequest.getRate() > 5) {
            errors.add("Rating must be between 1 and 5");
        }

        if (reviewRequest.getReview() != null && reviewRequest.getReview().length() > 1000) {
            errors.add("Review cannot exceed 1000 characters");
        }

        return errors;
    }

//    private RateAndReview convertToEntity(RateAndReviewDTO dto) {
//        Tourist tourist = new Tourist();
//        tourist.setId(dto.getTourist().getId());
//        tourist.setFirstName(dto.getTourist().getFirstName());
//        tourist.setLastName(dto.getTourist().getLastName());
//
//        return RateAndReview.builder()
//                .rate(dto.getRate())
//                .review(dto.getReview())
//                .serviceId(dto.getServiceId())
//                .tourist(tourist)
//                .createdDate(LocalDateTime.now())
//                .build();
//    }
//
//    private RateAndReviewDTO convertToDto(RateAndReview entity) {
//        TouristSummaryDTO touristDto = new TouristSummaryDTO();
//        touristDto.setId(entity.getTourist().getId());
//        touristDto.setFirstName(entity.getTourist().getFirstName());
//        touristDto.setLastName(entity.getTourist().getLastName());
//
//        return new RateAndReviewDTO();
//        // Or use setters if you don't have a builder for RateAndReviewDTO
//        RateAndReviewDTO dto = new RateAndReviewDTO();
//        dto.setId(entity.getId());
//        dto.setRate(entity.getRate());
//        dto.setReview(entity.getReview());
//        dto.setServiceId(entity.getServiceId());
//        dto.setTourist(touristDto);
//        dto.setCreatedDate(entity.getCreatedDate());
//
//        return dto;
//    }
//
//    @Override
//    public APIResponse<List<RateAndReviewDTO>> getReviewsByServiceId(Long serviceId) {
//        // Implementation to get reviews by service ID
//        List<RateAndReviewDTO> reviews = List.of(); // Replace with actual data
//        return APIResponse.<List<RateAndReviewDTO>>builder()
//                .success(true)
//                .message("Reviews retrieved successfully")
//                .data(reviews)
//                .build();
//    }
//
//    // Implement other methods similarly...
}
