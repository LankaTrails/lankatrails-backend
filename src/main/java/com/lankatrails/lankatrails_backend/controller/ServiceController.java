package com.lankatrails.lankatrails_backend.controller;

import com.lankatrails.lankatrails_backend.dtos.request.ServiceRequest;
import com.lankatrails.lankatrails_backend.model.Category;
import com.lankatrails.lankatrails_backend.model.Provider;
import com.lankatrails.lankatrails_backend.model.Service;
import com.lankatrails.lankatrails_backend.service.ServicesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/service")
public class ServiceController {
    @Autowired
    ServicesService servicesService;
    @PostMapping("/addService")
    public ResponseEntity<ServiceRequest>
            addService
            (@RequestBody Service service,
             @RequestBody Provider provider,
             @RequestBody Category category)
            {

                ServiceRequest serviceDTO=servicesService.addService(service,provider,category);
                return serviceDTO;
            }
}
