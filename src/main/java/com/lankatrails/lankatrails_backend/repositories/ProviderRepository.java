package com.lankatrails.lankatrails_backend.repositories;

import com.lankatrails.lankatrails_backend.model.Provider;
import com.lankatrails.lankatrails_backend.model.Tourist;
import com.lankatrails.lankatrails_backend.model.enums.ApprovalStatus;
import com.lankatrails.lankatrails_backend.model.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProviderRepository extends JpaRepository<Provider,Long> {
    Optional<Provider> findByUserId(Long userId);

    //Find all providers having any pending approval requests
    List<Provider> findByAccommodationApprovalStatusOrTourGuideApprovalStatusOrTransportApprovalStatusOrActivityApprovalStatusOrFoodApprovalStatus(
            ApprovalStatus accommodationStatus,
            ApprovalStatus tourGuideStatus,
            ApprovalStatus transportStatus,
            ApprovalStatus activityStatus,
            ApprovalStatus foodStatus
    );
}
