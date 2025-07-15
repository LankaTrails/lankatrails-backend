package com.lankatrails.lankatrails_backend.service;

import com.lankatrails.lankatrails_backend.dtos.request.ActivityServiceRequest;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.ActivityServiceResponse;
import com.lankatrails.lankatrails_backend.model.ActivityService;
import com.lankatrails.lankatrails_backend.model.PolicySection;
import com.lankatrails.lankatrails_backend.model.TabsSection;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ActivityServiceService {
    APIResponse<String> addService(ActivityServiceRequest activityService, List<MultipartFile> images);
    APIResponse<ActivityServiceResponse> getAll_ActivityServices(Integer pageNumber, Integer pageSize);
    APIResponse<ActivityServiceRequest> searchWithId(Long Id);
    APIResponse<ActivityServiceRequest> removeActivityService(Long Id);
    APIResponse<String> updateWithId(Long Id,ActivityServiceRequest activityService);
    ActivityServiceRequest addTabs(Long Id, TabsSection tabsSection);
    APIResponse<String> removeTabs(Long id);
    APIResponse<String> addNewPolicy(Long id, PolicySection policies);
    APIResponse<String> removePolicies(Long id);

}