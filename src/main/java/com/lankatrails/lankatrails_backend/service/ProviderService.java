package com.lankatrails.lankatrails_backend.service;

import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.BusinessDetailDTO;

public interface ProviderService {
    APIResponse<BusinessDetailDTO> getBusinessDetails();
}
