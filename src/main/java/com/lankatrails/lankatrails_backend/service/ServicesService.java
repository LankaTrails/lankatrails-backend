package com.lankatrails.lankatrails_backend.service;

import com.lankatrails.lankatrails_backend.dtos.request.ServiceRequest;
import com.lankatrails.lankatrails_backend.model.Services;

public interface ServicesService {
    ServiceRequest addService(Services services, Long category,Long provider );
}
