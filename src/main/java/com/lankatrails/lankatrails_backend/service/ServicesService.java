package com.lankatrails.lankatrails_backend.service;

import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.ProfilePicResponse;
import org.springframework.web.multipart.MultipartFile;

public interface ServicesService {
    APIResponse<String> addServiceImages(Long serviceId, MultipartFile[] serviceImages);
}
