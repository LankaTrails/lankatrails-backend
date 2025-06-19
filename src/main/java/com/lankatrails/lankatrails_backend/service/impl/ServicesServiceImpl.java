package com.lankatrails.lankatrails_backend.service.impl;

import com.lankatrails.lankatrails_backend.dtos.request.ServiceRequest;
import com.lankatrails.lankatrails_backend.exception.ResourceNotFoundException;
import com.lankatrails.lankatrails_backend.model.Category;
import com.lankatrails.lankatrails_backend.model.Provider;
import com.lankatrails.lankatrails_backend.model.Services;
import com.lankatrails.lankatrails_backend.repositories.CategoryRepository;
import com.lankatrails.lankatrails_backend.repositories.ProviderRepository;
import com.lankatrails.lankatrails_backend.service.ServicesService;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.config.ConfigDataResourceNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ServicesServiceImpl implements ServicesService {
    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProviderRepository providerRepository;

    @Autowired
    private ModelMapper modelMapper;


    @Override
    public ServiceRequest addService(Services services, Long categoryId,Long providerId) {

        Category category=categoryRepository.findById(categoryId)
                .orElseThrow(()->new ResourceNotFoundException("Category Not Found"));

        return null;
    }
}
