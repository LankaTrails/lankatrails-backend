package com.lankatrails.lankatrails_backend.service;

import com.lankatrails.lankatrails_backend.dtos.request.LicenseDTO;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.BusinessResponseDTO;
import com.lankatrails.lankatrails_backend.dtos.response.LicenseResponse;
import com.lankatrails.lankatrails_backend.dtos.response.LicenseResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

public interface ProviderService {
    APIResponse<BusinessResponseDTO> getBusinessDetails();

    APIResponse<String> licenseRenewal(List<LicenseDTO> licenseDTO, List<MultipartFile> licenseFiles);

    APIResponse<String> requestApproval(List<LicenseDTO> licenseDTO, List<MultipartFile> licenseFiles);

    APIResponse<LicenseResponseDTO> getLicenses();

    APIResponse<LicenseResponseDTO> getLicense(Long id);

    APIResponse<Set<LicenseResponse>> getLicenseForProvider();
}
