package com.lankatrails.lankatrails_backend.service;

import com.lankatrails.lankatrails_backend.dtos.request.ActivityServiceRequest;
import com.lankatrails.lankatrails_backend.dtos.response.ActivityServiceResponse;
import com.lankatrails.lankatrails_backend.model.ActivityService;
import com.lankatrails.lankatrails_backend.model.Services;

public interface ServicesService {
    ActivityServiceRequest addService(ActivityService activityService, Long category, Long provider );
    ActivityServiceResponse getAll_ActivityServices();
    ActivityServiceResponse searchWithId(Long Id);


//    ActivityServiceResponse searchById(Long id);
}