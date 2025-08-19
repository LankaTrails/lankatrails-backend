package com.lankatrails.lankatrails_backend.service;

import com.lankatrails.lankatrails_backend.dtos.request.LicenseDTO;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.BusinessDetailDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProviderService {
    APIResponse<BusinessDetailDTO> getBusinessDetails();
    APIResponse<String> licenseRenewal(List<LicenseDTO> licenseDTO,List<MultipartFile> licenseFiles);
}
