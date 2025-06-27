package com.lankatrails.lankatrails_backend.service;

import com.lankatrails.lankatrails_backend.dtos.request.ActivityServiceRequest;
import com.lankatrails.lankatrails_backend.dtos.response.ActivityServiceResponse;
import com.lankatrails.lankatrails_backend.model.ActivityService;
import com.lankatrails.lankatrails_backend.model.PolicySection;
import com.lankatrails.lankatrails_backend.model.Services;
import com.lankatrails.lankatrails_backend.model.TabsSection;

public interface ServicesService {
    ActivityServiceRequest addService(ActivityService activityService, Long provider );
    ActivityServiceResponse getAll_ActivityServices(Integer pageNumber, Integer pageSize);
    ActivityServiceRequest searchWithId(Long Id);
    ActivityServiceRequest removeActivityService(Long Id,ActivityService activityService);
    ActivityServiceRequest updateWithId(Long Id,ActivityServiceRequest activityService);
    ActivityServiceRequest addTabs(Long Id, TabsSection tabsSection);
    Boolean removeTabs(Long id);
    ActivityServiceRequest addNewPolicy(Long id, PolicySection policies);
    Boolean removePolicies(Long id);

}