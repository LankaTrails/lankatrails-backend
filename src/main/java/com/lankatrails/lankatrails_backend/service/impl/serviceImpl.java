package com.lankatrails.lankatrails_backend.service.impl;

import com.lankatrails.lankatrails_backend.exception.ResourceNotFoundException;
import com.lankatrails.lankatrails_backend.model.Services;
import com.lankatrails.lankatrails_backend.repositories.ServiceRepository;
import com.lankatrails.lankatrails_backend.service.ServicesForAll;
import org.springframework.beans.factory.annotation.Autowired;

@org.springframework.stereotype.Service
public class serviceImpl implements ServicesForAll {
    @Autowired
    ServiceRepository serviceRepository;
    public Boolean removeService(Long id){
        Services service=serviceRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Service",id));
        service.setStatus(false);
        return true;

    }
}
