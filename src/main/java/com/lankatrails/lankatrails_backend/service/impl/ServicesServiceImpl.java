package com.lankatrails.lankatrails_backend.service.impl;

import com.lankatrails.lankatrails_backend.dtos.request.ServiceRequest;
import com.lankatrails.lankatrails_backend.model.Category;
import com.lankatrails.lankatrails_backend.model.Provider;
import com.lankatrails.lankatrails_backend.service.ServicesService;
import org.springframework.stereotype.Service;

@Service
public class ServicesServiceImpl implements ServicesService {
    @Override
    public ServiceRequest addService(com.lankatrails.lankatrails_backend.model.Service service, Provider provider, Category category) {

        return null;
    }
}
