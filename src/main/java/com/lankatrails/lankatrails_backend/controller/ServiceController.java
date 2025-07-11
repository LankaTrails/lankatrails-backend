package com.lankatrails.lankatrails_backend.controller;

import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.ProfilePicResponse;
import com.lankatrails.lankatrails_backend.service.ServicesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/service")
public class ServiceController {
    @Autowired
    ServicesService servicesService;

    @PostMapping(value = "/{userId}/add-service-images", consumes = "multipart/form-data")
    public APIResponse<String> addServiceImages(@PathVariable Long userId, @RequestParam("serviceImages") MultipartFile[] serviceImages) {
        return servicesService.addServiceImages(userId, serviceImages);
    }

}
