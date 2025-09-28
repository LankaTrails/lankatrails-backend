package com.lankatrails.lankatrails_backend.service.impl;

import com.lankatrails.lankatrails_backend.dtos.request.ContactPersonDTO;
import com.lankatrails.lankatrails_backend.dtos.request.LicenseDTO;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.BusinessDetailDTO;
import com.lankatrails.lankatrails_backend.exception.ResourceNotFoundException;
import com.lankatrails.lankatrails_backend.model.Category;
import com.lankatrails.lankatrails_backend.model.License;
import com.lankatrails.lankatrails_backend.model.Provider;
import com.lankatrails.lankatrails_backend.model.enums.ApprovalStatus;
import com.lankatrails.lankatrails_backend.model.enums.ServiceCategory;
import com.lankatrails.lankatrails_backend.repositories.CategoryRepository;
import com.lankatrails.lankatrails_backend.repositories.LicenseRepository;
import com.lankatrails.lankatrails_backend.repositories.ProviderRepository;
import com.lankatrails.lankatrails_backend.security.utils.AuthUtils;
import com.lankatrails.lankatrails_backend.service.ProviderService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class ProviderServiceImpl implements ProviderService {
    @Autowired
    private AuthUtils authUtils;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private ProviderRepository providerRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private LicenseRepository licenseRepository;

    @Override
    public APIResponse<BusinessDetailDTO> getBusinessDetails() {
        BusinessDetailDTO businessDetail = new BusinessDetailDTO();
        // Assuming the provider is fetched from the AuthUtils
        Provider provider = providerRepository.findById(authUtils.loggedInUserId())
                .orElseThrow(() -> new RuntimeException("Provider not found with id: " + authUtils.loggedInUserId()));

        if (provider == null) {
            return APIResponse.<BusinessDetailDTO>builder()
                    .success(false)
                    .message("Provider not found")
                    .build();
        }

        // Populate business details from the provider
        businessDetail.setProviderId(provider.getUserId());
        businessDetail.setBusinessType(provider.getBusinessType());
        businessDetail.setBusinessRegistrationNumber(provider.getBusinessRegistrationNumber());
        businessDetail.setBusinessRegistrationUrl(provider.getBusinessRegistrationUrl());
        businessDetail.setContactPerson(modelMapper.map(provider.getContactPerson(), ContactPersonDTO.class));

        return APIResponse.<BusinessDetailDTO>builder()
                .success(true)
                .message("Business details retrieved successfully")
                .data(businessDetail)
                .build();
    }

    @Override
    public APIResponse<String> licenseRenewal(List<LicenseDTO> licenseDTO, List<MultipartFile> licenseFiles) {
        Provider provider = providerRepository.findByUserId(authUtils.loggedInUserId()).orElseThrow(() -> new ResourceNotFoundException("Provider", authUtils.loggedInUserId()));
        //Add all the licenses to the license repository
        if (!licenseDTO.isEmpty()) {
            for (LicenseDTO license : licenseDTO) {
                Category category = categoryRepository.findByCategoryName(license.getCategory()).orElseThrow(() -> new ResourceNotFoundException("Category", license.getCategory().getDisplayName()));
                License setLicense = new License();
                setLicense.setLicenseNumber(license.getLicenseNumber());
                setLicense.setExpiryDate(license.getExpiryDate());
                setLicense.setLicenseUrl(license.getLicenseUrl());
                setLicense.setCategory(category);
                setLicense.setProvider(provider);

                //Update the status of the provider's respective category to RENEWAL
                if (category.getCategoryName() == ServiceCategory.ACCOMMODATION) {
                    provider.setAccommodationApprovalStatus(ApprovalStatus.RENEWAL);
                } else if (category.getCategoryName() == ServiceCategory.TOUR_GUIDE) {
                    provider.setTourGuideApprovalStatus(ApprovalStatus.RENEWAL);
                } else if (category.getCategoryName() == ServiceCategory.TRANSPORT) {
                    provider.setTransportApprovalStatus(ApprovalStatus.RENEWAL);
                } else if (category.getCategoryName() == ServiceCategory.FOOD_BEVERAGE) {
                    provider.setFoodApprovalStatus(ApprovalStatus.RENEWAL);
                } else if (category.getCategoryName() == ServiceCategory.ACTIVITY) {
                    provider.setActivityApprovalStatus(ApprovalStatus.RENEWAL);
                }
                //save the license
                licenseRepository.save(setLicense);
                //update the provider entity
                providerRepository.save(provider);

            }
            return APIResponse.<String>builder()
                    .success(true)
                    .message("Licenses provided for verification")
                    .data("")
                    .build();

        } else {
            return APIResponse.<String>builder()
                    .success(false)
                    .message("No licenses added")
                    .data("")
                    .build();
        }


    }
}
