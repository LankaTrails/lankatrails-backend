package com.lankatrails.lankatrails_backend.service.impl;

import com.lankatrails.lankatrails_backend.dtos.request.ContactPersonDTO;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.BusinessDetailDTO;
import com.lankatrails.lankatrails_backend.model.Provider;
import com.lankatrails.lankatrails_backend.security.utils.AuthUtils;
import com.lankatrails.lankatrails_backend.service.ProviderService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProviderServiceImpl implements ProviderService {
    @Autowired
    private AuthUtils authUtils;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public APIResponse<BusinessDetailDTO> getBusinessDetails() {
        BusinessDetailDTO businessDetail = new BusinessDetailDTO();
        // Assuming the provider is fetched from the AuthUtils
        Provider provider = (Provider) authUtils.loggedInUser();

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
}
