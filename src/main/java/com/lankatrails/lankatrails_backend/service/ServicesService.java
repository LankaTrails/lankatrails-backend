package com.lankatrails.lankatrails_backend.service;

import com.lankatrails.lankatrails_backend.dtos.request.ServiceRequest;
import com.lankatrails.lankatrails_backend.model.Category;
import com.lankatrails.lankatrails_backend.model.Provider;
import com.lankatrails.lankatrails_backend.model.Service;

public interface ServicesService {
    ServiceRequest addService(Service service, Provider provider, Category category);
}
