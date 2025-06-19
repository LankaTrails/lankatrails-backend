package com.lankatrails.lankatrails_backend.controller;

import com.lankatrails.lankatrails_backend.dtos.request.ServiceRequest;
import com.lankatrails.lankatrails_backend.model.Services;
import com.lankatrails.lankatrails_backend.service.ServicesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/service")
public class ServiceController {
    @Autowired
    ServicesService servicesService;

    @PostMapping("/addService/{categoryId}/{providerId}")
    public ResponseEntity<ServiceRequest> addService
            (
             @RequestBody Services service,
             @PathVariable Long categoryId,
             @PathVariable Long providerId
             )
            {
                ServiceRequest serviceDTO=servicesService.addService(service,categoryId,providerId);
                return serviceDTO;
            }
}
