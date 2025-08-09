package com.lankatrails.lankatrails_backend.service;


import com.lankatrails.lankatrails_backend.dtos.request.FoodBeverageRequest;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.ActivityServiceResponse;
import com.lankatrails.lankatrails_backend.dtos.response.FoodBeverageResponse;
import com.lankatrails.lankatrails_backend.model.PolicySection;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FoodBeverageService {
    APIResponse<String> addService(FoodBeverageRequest foodBeverageRequest, List<MultipartFile> images);
    APIResponse<FoodBeverageResponse> getAll(Integer pageNumber, Integer pageSize);
    APIResponse<FoodBeverageRequest> searchWithId(Long Id);
    APIResponse<String> addNewPolicy(PolicySection policies);
    APIResponse<String> updateService(Long id, FoodBeverageRequest foodBeverageRequest, List<MultipartFile> images);

}
