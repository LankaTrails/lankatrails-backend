package com.lankatrails.lankatrails_backend.factory;

import com.lankatrails.lankatrails_backend.model.*;
import com.lankatrails.lankatrails_backend.model.enums.*;
import com.lankatrails.lankatrails_backend.dtos.request.*;
import com.lankatrails.lankatrails_backend.repositories.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class UserFactory {

    private final CategoryRepository categoryRepository;
    private final LocationFactory locationFactory;
    private final ContactPersonFactory contactPersonFactory;
    private final LicenseFactory licenseFactory;

    public User createUser(TouristRegistrationRequest request) {
        Tourist tourist = new Tourist();
        tourist.setEmail(request.getEmail().toLowerCase());
        tourist.setFirstName(request.getFirstName());
        tourist.setLastName(request.getLastName());
        tourist.setCountry(request.getCountry().toLowerCase());
        tourist.setPhoneNumber(request.getPhoneNumber());
        tourist.setRole(UserRole.ROLE_TOURIST);
        return tourist;
    }

    public User createUser(ProviderRegistrationRequest request) {
        Provider provider = new Provider();

        // Set basic info
        provider.setEmail(request.getEmail().toLowerCase());
        provider.setRole(UserRole.ROLE_PROVIDER);
        provider.setStatus(UserStatus.PENDING);
        provider.setProfilePictureUrl(request.getProfilePictureUrl());

        // Set provider-specific info
        provider.setBusinessName(request.getBusinessName());
        provider.setBusinessDescription(request.getBusinessDescription());
        provider.setBusinessType(request.getBusinessType());

        // Set business info
        provider.setBusinessRegistrationNumber(request.getBusinessRegistrationNumber());
        provider.setBusinessRegistrationUrl(request.getBusinessRegistrationUrl());
        provider.setCoverImageUrl(request.getCoverImageUrl());

        // Set approval statuses
        provider.setAccommodationApprovalStatus(request.getAccommodationApprovalStatus());
        provider.setTourGuideApprovalStatus(request.getTourGuideApprovalStatus());
        provider.setTransportApprovalStatus(request.getTransportApprovalStatus());
        provider.setActivityApprovalStatus(request.getActivityApprovalStatus());
        provider.setFoodApprovalStatus(request.getFoodApprovalStatus());

        // Create and set related entities using dedicated factories
        provider.setLocation(locationFactory.createFromDTO(request.getLocation()));
        provider.setContactPerson(contactPersonFactory.createFromDTO(request.getContactPerson()));

        // Add licenses
        if (request.getLicenses() != null) {
            request.getLicenses().forEach(licenseDTO ->
                    provider.addLicense(licenseFactory.createFromDTO(licenseDTO))
            );
        }

        return provider;
    }

    private ApprovalStatus parseApprovalStatus(ApprovalStatus status) {
        if (status == null) {
            return ApprovalStatus.NOT_REQUESTED;
        }
        try {
            return ApprovalStatus.valueOf(status.name());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid approval status: " + status);
        }
    }

    private Set<Category> mapCategories(Set<String> categoryNames) {
        Set<Category> categories = new HashSet<>();

        categoryNames.forEach(categoryName -> {
            ServiceCategory serviceCategory = ServiceCategory.valueOf(categoryName.toUpperCase());
            Category category = categoryRepository.findByCategoryName(serviceCategory)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid category: " + categoryName));
            categories.add(category);
        });

        return categories;
    }
}