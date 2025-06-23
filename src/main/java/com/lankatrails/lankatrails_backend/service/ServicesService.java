package com.lankatrails.lankatrails_backend.service;

import com.lankatrails.lankatrails_backend.dtos.request.ActivityServiceRequest;
import com.lankatrails.lankatrails_backend.dtos.response.ActivityServiceResponse;
import com.lankatrails.lankatrails_backend.model.ActivityService;
import com.lankatrails.lankatrails_backend.model.Services;

public interface ServicesService {
    ActivityServiceRequest addService(ActivityService activityService, Long category, Long provider );
    ActivityServiceResponse getAll_ActivityServices();
    ActivityServiceRequest searchWithId(Long Id);
    ActivityServiceRequest removeActivityService(Long Id,ActivityService activityService);
    ActivityServiceRequest updateWithId(Long Id,ActivityService activityService);

}