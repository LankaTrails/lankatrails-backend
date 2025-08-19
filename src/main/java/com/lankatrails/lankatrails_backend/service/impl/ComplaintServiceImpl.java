package com.lankatrails.lankatrails_backend.service.impl;

import com.lankatrails.lankatrails_backend.dtos.request.ComplaintDTO;
import com.lankatrails.lankatrails_backend.dtos.request.ComplaintImgDTO;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.exception.ResourceNotFoundException;
import com.lankatrails.lankatrails_backend.model.Complaint;
import com.lankatrails.lankatrails_backend.model.ComplaintImage;
import com.lankatrails.lankatrails_backend.model.Service;
import com.lankatrails.lankatrails_backend.model.Tourist;
import com.lankatrails.lankatrails_backend.model.enums.ComplaintStatus;
import com.lankatrails.lankatrails_backend.repositories.ComplaintImgRepository;
import com.lankatrails.lankatrails_backend.repositories.ComplaintRepository;
import com.lankatrails.lankatrails_backend.repositories.ServiceRepository;
import com.lankatrails.lankatrails_backend.repositories.TouristRepository;
import com.lankatrails.lankatrails_backend.security.utils.AuthUtils;
import com.lankatrails.lankatrails_backend.service.ComplaintService;
import org.springframework.beans.factory.annotation.Autowired;

public class ComplaintServiceImpl implements ComplaintService {
    @Autowired
    ComplaintRepository complaintRepository;

    @Autowired
    ServiceRepository serviceRepository;

    @Autowired
    TouristRepository touristRepository;

    @Autowired
    ComplaintImgRepository complaintImgRepository;

    @Autowired
    AuthUtils authUtils;

    @Override
    public APIResponse<String> addNewComplaint(ComplaintDTO complaintDTO) {
        Service service = serviceRepository.findById(complaintDTO.getServiceId())
                .orElseThrow(()->new ResourceNotFoundException("Service",complaintDTO.getServiceId()));
        Tourist tourist =touristRepository.findByUserId(authUtils.loggedInUserId())
                .orElseThrow(()-> new ResourceNotFoundException("Tourist",authUtils.loggedInUserId()));

        //Prepare the complaint
        Complaint complaint = new Complaint();
        complaint.setDescription(complaintDTO.getDescription());
        complaint.setComplaintStatus(ComplaintStatus.PENDING);
        complaint.setService(service);
        complaint.setTourist(tourist);
        //save the complaint
        Complaint savedComplaint = complaintRepository.save(complaint);
        //prepare the complaint images
        for(ComplaintImgDTO complaintImg : complaintDTO.getComplaintImgs()){
            ComplaintImage img = new ComplaintImage();
            img.setImageUrl(complaintImg.getImageUrl());
            img.setComplaint(savedComplaint);
            //save the image
            complaintImgRepository.save(img);

        }

        return APIResponse.<String>builder()
                .success(true)
                .message("Complaint Added successfully")
                .data("")
                .build();
    }
}
