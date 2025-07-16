package com.lankatrails.lankatrails_backend.service.impl;

import com.lankatrails.lankatrails_backend.dtos.request.ActivityServiceRequest;
import com.lankatrails.lankatrails_backend.dtos.request.ServiceRequest;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.exception.BadRequestException;
import com.lankatrails.lankatrails_backend.exception.ResourceNotFoundException;
import com.lankatrails.lankatrails_backend.model.ActivityService;
import com.lankatrails.lankatrails_backend.model.Image;
import com.lankatrails.lankatrails_backend.model.Service;
import com.lankatrails.lankatrails_backend.model.enums.UploadCategory;
import com.lankatrails.lankatrails_backend.repositories.ImageRepository;
import com.lankatrails.lankatrails_backend.repositories.ServiceRepository;
import com.lankatrails.lankatrails_backend.service.ServicesService;
import com.lankatrails.lankatrails_backend.service.utils.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.Set;

@org.springframework.stereotype.Service
public class ServicesServiceImpl implements ServicesService {
    @Autowired
    ServiceRepository serviceRepository;

    @Autowired
    ImageRepository imageRepository;
    @Autowired
    FileUploadService fileUploadService;

    @Override
    public APIResponse<String> addServiceImages(Long serviceId, MultipartFile[] serviceImages) {
        if (serviceImages == null || serviceImages.length == 0) {
            throw new BadRequestException("Service images cannot be null or empty", "ServiceImages", null);
        }

        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("serviceID", serviceId));

        Set<Image> images = new HashSet<>(fileUploadService.storeImages(serviceImages, UploadCategory.SERVICE_PICTURE));

        for(Image img : images){
            img.setService(service);
            imageRepository.save(img);
        }

        return  APIResponse.<String>builder()
                .success(true)
                .message("Successfully Added")
                .data("")
                .build();
    }

//    @Override
//    public APIResponse<ServiceRequest> removeAService(Long Id){
//        Service service=serviceRepository.findById(Id)
//                .orElseThrow(()->new ResourceNotFoundException("Activity Service",Id));
//        service.setStatus(false);
//        Service updatedService=serviceRepository.save(service);
//
//        ServiceRequest activityServiceResponse=new ServiceRequest();
//        activityServiceResponse.setServiceName(activityService.getServiceName());
//        activityServiceResponse.setServiceId(activityService.getServiceId());
//        activityServiceResponse.setStatus(activityService.getStatus());
//
//        return APIResponse.<ActivityServiceRequest>builder()
//                .success(true)
//                .message("Successfully Deleted")
//                .data(activityServiceResponse)
//                .build();
//
//    }



}
