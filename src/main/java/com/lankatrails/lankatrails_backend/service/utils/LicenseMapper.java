package com.lankatrails.lankatrails_backend.service.utils;

import com.lankatrails.lankatrails_backend.dtos.request.LicenseDTO;
import com.lankatrails.lankatrails_backend.model.License;
import com.lankatrails.lankatrails_backend.model.Provider;
import com.lankatrails.lankatrails_backend.model.enums.ApprovalStatus;
import com.lankatrails.lankatrails_backend.model.enums.ServiceCategory;

/** Maps licences to DTOs, including the provider's approval status for the licence's category. */
public final class LicenseMapper {

    private LicenseMapper() {
    }

    public static ApprovalStatus approvalStatusFor(Provider provider, ServiceCategory category) {
        if (provider == null || category == null) {
            return null;
        }
        return switch (category) {
            case ACCOMMODATION -> provider.getAccommodationApprovalStatus();
            case TRANSPORT -> provider.getTransportApprovalStatus();
            case FOOD_BEVERAGE -> provider.getFoodApprovalStatus();
            case ACTIVITY -> provider.getActivityApprovalStatus();
            case TOUR_GUIDE -> provider.getTourGuideApprovalStatus();
        };
    }

    public static LicenseDTO toDto(License license) {
        Provider provider = license.getProvider();
        ServiceCategory category = license.getCategory() != null ? license.getCategory().getCategoryName() : null;

        LicenseDTO dto = new LicenseDTO();
        dto.setLicenseId(license.getLicenseId());
        dto.setLicenseNumber(license.getLicenseNumber());
        dto.setExpiryDate(license.getExpiryDate());
        dto.setLicenseUrl(license.getLicenseUrl());
        dto.setCategory(category);
        dto.setCategoryName(category != null ? category.getDisplayName() : null);
        dto.setStatus(approvalStatusFor(provider, category));
        if (provider != null) {
            dto.setProviderId(provider.getUserId());
            dto.setBusinessName(provider.getBusinessName());
        }
        return dto;
    }
}
