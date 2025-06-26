package com.lankatrails.lankatrails_backend.controller;

import com.lankatrails.lankatrails_backend.dtos.request.ActivityServiceRequest;
import com.lankatrails.lankatrails_backend.model.ActivityService;
import com.lankatrails.lankatrails_backend.model.Services;
import com.lankatrails.lankatrails_backend.service.ServicesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/activityService")
public class ActivityServiceController {
    @Autowired
    ServicesService servicesService;

    @PostMapping("/add/{categoryId}/{providerId}")
    public ResponseEntity<ActivityServiceRequest> addService
            (
                    @RequestBody ActivityService service,
                    @PathVariable Long categoryId,
                    @PathVariable Long providerId
            ){
               ActivityServiceRequest ActivityServiceDTO =  servicesService.addService(service,categoryId,providerId);
               //return ResponseEntity.status(HttpStatus.CREATED).body(ActivityServiceDTO);
               return new ResponseEntity<>(ActivityServiceDTO,HttpStatus.CREATED);
    }
}
