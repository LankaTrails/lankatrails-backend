package com.lankatrails.lankatrails_backend.service;

import com.lankatrails.lankatrails_backend.dtos.request.AccommodationServiceRequestDTO;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AccommodationService {
    APIResponse<String> addService(AccommodationServiceRequestDTO accommodationService, List<MultipartFile> images);
}
