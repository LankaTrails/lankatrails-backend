package com.lankatrails.lankatrails_backend.service.impl;

import com.lankatrails.lankatrails_backend.dtos.request.ComplaintDTO;
import com.lankatrails.lankatrails_backend.dtos.request.ComplaintImgDTO;
import com.lankatrails.lankatrails_backend.dtos.request.ComplaintInfoDTO;
import com.lankatrails.lankatrails_backend.dtos.request.ComplaintViewDTO;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.ComplaintInfoResponse;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@org.springframework.stereotype.Service
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
    @Transactional
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

    @Override
    @Transactional
    public APIResponse<ComplaintInfoResponse> getAllComplaints() {
        List<Complaint> complaints = complaintRepository.findByComplaintStatus(ComplaintStatus.PENDING);
        if (!complaints.isEmpty()){
            List<ComplaintInfoDTO> responseList = new ArrayList<>();
            for (Complaint complaint : complaints){
                Service service = serviceRepository.findById(complaint.getService().getServiceId())
                        .orElseThrow(()->new ResourceNotFoundException("Service",complaint.getService().getServiceId()));

                Tourist tourist = touristRepository.findByUserId(authUtils.loggedInUserId())
                        .orElseThrow(()->new ResourceNotFoundException("Tourist", authUtils.loggedInUserId()));
                ComplaintInfoDTO complaintInfoDTO = new ComplaintInfoDTO();
                complaintInfoDTO.setBusinessName(service.getServiceName());
                complaintInfoDTO.setComplaintStatus(complaint.getComplaintStatus());
                complaintInfoDTO.setTouristEmail(tourist.getEmail());
                complaintInfoDTO.setBusinessType(service.getProvider().getBusinessType());

                //add to responseList
                responseList.add(complaintInfoDTO);
            }
            ComplaintInfoResponse complaintInfoResponse = new ComplaintInfoResponse();
            complaintInfoResponse.setContent(responseList);

            return APIResponse.<ComplaintInfoResponse>builder()
                    .success(true)
                    .message("Loaded all the status pending complaints")
                    .data(complaintInfoResponse)
                    .build();
        }else{
            return APIResponse.<ComplaintInfoResponse>builder()
                    .success(true)
                    .message("No pending complaints to resolve")
                    .data(new ComplaintInfoResponse())
                    .build();
        }

    }

    @Override
    @Transactional
    public APIResponse<ComplaintViewDTO> viewOneComplaint(Long id) {
        Complaint complaint= complaintRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Complaint",id));
        Service service = serviceRepository.findById(complaint.getService().getServiceId())
                .orElseThrow(()-> new ResourceNotFoundException("Service",complaint.getService().getServiceId()));

        Tourist tourist = touristRepository.findByUserId(complaint.getTourist().getUserId())
                .orElseThrow(()-> new ResourceNotFoundException("Tourist",complaint.getTourist().getUserId()));

        List<Complaint> noOfComplaints = complaintRepository.findByService_ServiceId(service.getServiceId());

        ComplaintViewDTO complaintViewDTO = new ComplaintViewDTO();

        complaintViewDTO.setTouristEmail(tourist.getEmail());
        complaintViewDTO.setDescription(complaint.getDescription());
        complaintViewDTO.setBusinessType(service.getProvider().getBusinessType());
        complaintViewDTO.setUserStatus(tourist.getStatus());
        complaintViewDTO.setServiceName(service.getServiceName());
        complaintViewDTO.setTotalComplaints(noOfComplaints.size());
        complaintViewDTO.setCategory(service.getCategory());


        return APIResponse.<ComplaintViewDTO>builder()
                .success(true)
                .message("View complaint Details")
                .data(complaintViewDTO)
                .build();
    }
}
