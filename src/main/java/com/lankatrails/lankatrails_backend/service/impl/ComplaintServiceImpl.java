package com.lankatrails.lankatrails_backend.service.impl;

import com.lankatrails.lankatrails_backend.dtos.request.*;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.ComplaintInfoResponse;
import com.lankatrails.lankatrails_backend.exception.ResourceNotFoundException;
import com.lankatrails.lankatrails_backend.model.*;
import com.lankatrails.lankatrails_backend.model.enums.ComplaintResult;
import com.lankatrails.lankatrails_backend.model.enums.ComplaintStatus;
import com.lankatrails.lankatrails_backend.model.enums.ComplaintType;
import com.lankatrails.lankatrails_backend.repositories.*;
import com.lankatrails.lankatrails_backend.security.utils.AuthUtils;
import com.lankatrails.lankatrails_backend.service.ComplaintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    BookingRepository bookingRepository;

    @Autowired
    ComplaintResolveRepository complaintResolveRepository;

    @Autowired
    ComplaintRejectRepository complaintRejectRepository;

    @Autowired
    WarningRepository warningRepository;

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
        complaint.setDateTime(LocalDateTime.now());
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
        List<Complaint> complaints = complaintRepository.findByComplaintStatusOrComplaintStatus(ComplaintStatus.PENDING,ComplaintStatus.IN_PROGRESS);
        if (!complaints.isEmpty()){
            List<ComplaintInfoDTO> responseList = new ArrayList<>();
            for (Complaint complaint : complaints){
                Service service = serviceRepository.findById(complaint.getService().getServiceId())
                        .orElseThrow(()->new ResourceNotFoundException("Service",complaint.getService().getServiceId()));

                Tourist tourist = touristRepository.findByUserId(complaint.getTourist().getUserId())
                        .orElseThrow(()->new ResourceNotFoundException("Tourist", complaint.getTourist().getUserId()));
                ComplaintInfoDTO complaintInfoDTO = new ComplaintInfoDTO();
                complaintInfoDTO.setBusinessName(service.getServiceName());
                complaintInfoDTO.setComplaintStatus(complaint.getComplaintStatus());
                complaintInfoDTO.setTouristEmail(tourist.getEmail());
                complaintInfoDTO.setBusinessType(service.getProvider().getBusinessType());
                complaintInfoDTO.setComplaintId(complaint.getComplaintId());
                complaintInfoDTO.setUserStatus(tourist.getStatus());
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

        List<Booking> bookings = bookingRepository.findByService_ServiceIdAndTourist_UserId(service.getServiceId(),tourist.getUserId());

        Booking booking = bookingRepository.findById(complaint.getBooking().getBookingId())
                .orElseThrow(()-> new ResourceNotFoundException("Booking",complaint.getBooking().getBookingId()));

        List<Complaint> noOfComplaints = complaintRepository.findByService_ServiceId(service.getServiceId());

        ComplaintViewDTO complaintViewDTO = new ComplaintViewDTO();

        CategoryRequestDTO categoryRequestDTO = new CategoryRequestDTO();
        categoryRequestDTO.setCategoryName(service.getCategory().getCategoryName());

//        BookingRequestDTO bookingRequestDTO = new BookingRequestDTO();


        complaintViewDTO.setTouristEmail(tourist.getEmail());
        complaintViewDTO.setDescription(complaint.getDescription());
        complaintViewDTO.setBusinessType(service.getProvider().getBusinessType());
        complaintViewDTO.setUserStatus(tourist.getStatus());
        complaintViewDTO.setServiceName(service.getServiceName());
        complaintViewDTO.setTotalComplaints(noOfComplaints.size());
        complaintViewDTO.setCategory(categoryRequestDTO);
        complaintViewDTO.setComplaintId(complaint.getComplaintId());
        complaintViewDTO.setComplaintDateTime(complaint.getDateTime());
        complaintViewDTO.setBookingId(complaint.getBooking().getBookingId());
//        complaintViewDTO.setBookings(bookings);

        return APIResponse.<ComplaintViewDTO>builder()
                .success(true)
                .message("View complaint Details")
                .data(complaintViewDTO)
                .build();
    }

    @Override
    public APIResponse<String> handleComplaint(ComplaintHandleRequestDTO complaintHandleRequestDTO) {
        Service service = serviceRepository.findById(complaintHandleRequestDTO.getServiceId())
                .orElseThrow(()->new ResourceNotFoundException("Service", complaintHandleRequestDTO.getServiceId()));
        Tourist tourist = touristRepository.findByUserId(complaintHandleRequestDTO.getUserId())
                .orElseThrow(()-> new ResourceNotFoundException("Tourist", complaintHandleRequestDTO.getUserId()));
        Complaint complaint = complaintRepository.findById(complaintHandleRequestDTO.getComplaintId())
                .orElseThrow(()->new ResourceNotFoundException("Complaint", complaintHandleRequestDTO.getComplaintId()));

        complaint.setComplaintStatus(ComplaintStatus.RESOLVED);

        if (complaintHandleRequestDTO.getComplaintType()== ComplaintType.PROVIDER_FAULT && complaintHandleRequestDTO.getWarningStatus()){
            //increase the no of warnings if it is a provider's fault
            service.setWarnings(service.getWarnings() + 1);
            //save the warning
            Warning warning = new Warning();
            warning.setWarning(complaintHandleRequestDTO.getWarningReason());
            warning.setService(service);
            warningRepository.save(warning);
        }

        if(complaintHandleRequestDTO.getComplaintResult() == ComplaintResult.REFUND_FROM_PROVIDER){
            //get the resolved criteria set
            ComplaintResolve complaintResolve = complaintResolveRepository.findById(complaintHandleRequestDTO.getResolveId())
                    .orElseThrow(()->new ResourceNotFoundException("Complaint Resolve", complaintHandleRequestDTO.getServiceId()));
            //Set the resolved category
            complaint.setComplaintResolve(complaintResolve);
            //refund the provider's money
        }else if (complaintHandleRequestDTO.getComplaintResult() == ComplaintResult.REFUND_FROM_COMPANY){
            //get the resolved criteria set
            ComplaintResolve complaintResolve = complaintResolveRepository.findById(complaintHandleRequestDTO.getResolveId())
                    .orElseThrow(()->new ResourceNotFoundException("Complaint Resolve", complaintHandleRequestDTO.getServiceId()));
            //Set the resolved category
            complaint.setComplaintResolve(complaintResolve);
            //refund the entire amount from the company
        }else if (complaintHandleRequestDTO.getComplaintResult() == ComplaintResult.REJECT){
            //get the rejected criteria set
            ComplaintReject complaintReject = complaintRejectRepository.findById(complaintHandleRequestDTO.getRejectId())
                    .orElseThrow(()->new ResourceNotFoundException("Complaint Resolve", complaintHandleRequestDTO.getServiceId()));
            //Set why rejected
            complaint.setComplaintReject(complaintReject);
            //No need of refund

        }
        complaintRepository.save(complaint);

        return null;
    }

    @Override
    @Transactional
    public APIResponse<String> updateProgress(Long id, ComplaintViewDTO complaintViewDTO) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Complaint",id));
        complaint.setComplaintStatus(ComplaintStatus.IN_PROGRESS);
        complaint.setInvestigationStartedDate(complaintViewDTO.getInvestigationStartedDate());
        complaintRepository.save(complaint);
        return APIResponse.<String>builder()
                .success(true)
                .message("Successfully Updated")
                .data("")
                .build();
    }
}
