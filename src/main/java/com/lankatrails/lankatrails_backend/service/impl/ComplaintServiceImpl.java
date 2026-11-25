package com.lankatrails.lankatrails_backend.service.impl;

import com.lankatrails.lankatrails_backend.service.payment.ComplaintRefundService;
import com.lankatrails.lankatrails_backend.exception.BadRequestException;
import com.lankatrails.lankatrails_backend.exception.ConflictException;
import com.lankatrails.lankatrails_backend.model.enums.RefundStatus;
import com.lankatrails.lankatrails_backend.dtos.request.*;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.ComplaintInfoResponse;
import com.lankatrails.lankatrails_backend.exception.ResourceNotFoundException;
import com.lankatrails.lankatrails_backend.model.*;
import com.lankatrails.lankatrails_backend.model.enums.ComplaintResult;
import com.lankatrails.lankatrails_backend.model.enums.ComplaintStatus;
import com.lankatrails.lankatrails_backend.model.enums.ComplaintType;
import com.lankatrails.lankatrails_backend.repositories.*;
import com.lankatrails.lankatrails_backend.security.jwt.JwtUtils;
import com.lankatrails.lankatrails_backend.security.utils.AuthUtils;
import com.lankatrails.lankatrails_backend.service.ComplaintService;
import com.lankatrails.lankatrails_backend.service.utils.EmailService;
import io.vavr.API;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@org.springframework.stereotype.Service
public class ComplaintServiceImpl implements ComplaintService {
    private static final Logger log = LoggerFactory.getLogger(ComplaintServiceImpl.class);
    @Autowired
    private EmailService emailService;

    @Autowired
    ComplaintRepository complaintRepository;

    @Autowired
    ProviderRepository providerRepository;

    @Autowired
    ServiceRepository serviceRepository;

    @Autowired
    TouristRepository touristRepository;

    @Autowired
    UserRepository userRepository;

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

    @Autowired
    ComplaintRefundService complaintRefundService;

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
        // Link the booking (needed to refund it later); it must be the tourist's own booking of this service
        if (complaintDTO.getBookingId() != null) {
            Booking booking = bookingRepository.findById(complaintDTO.getBookingId())
                    .orElseThrow(() -> new ResourceNotFoundException("Booking", complaintDTO.getBookingId()));
            boolean ownBooking = booking.getTripParticipant() != null
                    && booking.getTripParticipant().getTourist() != null
                    && tourist.getUserId().equals(booking.getTripParticipant().getTourist().getUserId());
            boolean sameService = booking.getTripItem() != null && booking.getTripItem().getService() != null
                    && service.getServiceId().equals(booking.getTripItem().getService().getServiceId());
            if (!ownBooking || !sameService) {
                throw new BadRequestException("Booking does not belong to this tourist and service", "bookingId", complaintDTO.getBookingId());
            }
            complaint.setBooking(booking);
        }
        //save the complaint
        Complaint savedComplaint = complaintRepository.save(complaint);
        //prepare the complaint images
        for(ComplaintImgDTO complaintImg : Optional.ofNullable(complaintDTO.getComplaintImgs()).orElse(List.of())){
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
    public APIResponse<String> addNewGeneralComplaint(ComplaintDTO complaintDTO) {
        Tourist tourist = touristRepository.findByUserId(authUtils.loggedInUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Tourist", authUtils.loggedInUserId()));

        // Prepare the general complaint (without service)
        Complaint complaint = new Complaint();
        complaint.setDescription(complaintDTO.getDescription());
        complaint.setComplaintStatus(ComplaintStatus.PENDING);
        complaint.setService(null); // General complaint - no specific service
        complaint.setTourist(tourist);
        complaint.setDateTime(LocalDateTime.now());
        
        // Save the complaint
        Complaint savedComplaint = complaintRepository.save(complaint);
        
        // Prepare the complaint images
        if (complaintDTO.getComplaintImgs() != null && !complaintDTO.getComplaintImgs().isEmpty()) {
            for (ComplaintImgDTO complaintImg : complaintDTO.getComplaintImgs()) {
                ComplaintImage img = new ComplaintImage();
                img.setImageUrl(complaintImg.getImageUrl());
                img.setComplaint(savedComplaint);
                // Save the image
                complaintImgRepository.save(img);
            }
        }

        return APIResponse.<String>builder()
                .success(true)
                .message("General complaint submitted successfully")
                .data("")
                .build();
    }

    @Override
    @Transactional
    public APIResponse<List<ComplaintViewDTO>> getMyComplaints() {
        try {
            // Get the currently logged-in tourist
            Tourist tourist = touristRepository.findByUserId(authUtils.loggedInUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Tourist", authUtils.loggedInUserId()));
            
            // Get all complaints for this tourist
            List<Complaint> complaints = complaintRepository.findByTouristOrderByDateTimeDesc(tourist);
            
            if (complaints.isEmpty()) {
                return APIResponse.<List<ComplaintViewDTO>>builder()
                        .success(true)
                        .message("No complaints found")
                        .data(new ArrayList<>())
                        .build();
            }
            
            List<ComplaintViewDTO> complaintViewDTOs = new ArrayList<>();
            
            for (Complaint complaint : complaints) {
                ComplaintViewDTO dto = new ComplaintViewDTO();
                dto.setComplaintId(complaint.getComplaintId());
                dto.setDescription(complaint.getDescription());
                dto.setComplaintDateTime(complaint.getDateTime());
                dto.setComplaintStatus(complaint.getComplaintStatus());
                dto.setComplaintResult(complaint.getComplaintResult());
                
                // Set service information if available
                if (complaint.getService() != null) {
                    dto.setServiceName(complaint.getService().getServiceName());
                }
                
                // Set complaint images
                List<ComplaintImage> complaintImgs = complaintImgRepository.findByComplaint(complaint);
                if (!complaintImgs.isEmpty()) {
                    List<String> imageUrls = complaintImgs.stream()
                            .map(ComplaintImage::getImageUrl)
                            .toList();
                    dto.setComplaintImgs(imageUrls);
                }
                
                // Set admin feedback if complaint is resolved
                if (complaint.getComplaintStatus() == ComplaintStatus.RESOLVED) {
                    // Try to get admin feedback from resolve table
                    // Note: You may need to adjust this based on your actual table structure
                    dto.setAdminToTourist("Your complaint has been resolved");
                }
                
                complaintViewDTOs.add(dto);
            }
            
            return APIResponse.<List<ComplaintViewDTO>>builder()
                    .success(true)
                    .message("Complaints retrieved successfully")
                    .data(complaintViewDTOs)
                    .build();
                    
        } catch (ResourceNotFoundException e) {
            return APIResponse.<List<ComplaintViewDTO>>builder()
                    .success(false)
                    .message(e.getMessage())
                    .data(new ArrayList<>())
                    .build();
        } catch (Exception e) {
            return APIResponse.<List<ComplaintViewDTO>>builder()
                    .success(false)
                    .message("Failed to retrieve complaints: " + e.getMessage())
                    .data(new ArrayList<>())
                    .build();
        }
    }

    @Override
    @Transactional
    public APIResponse<ComplaintInfoResponse> getAllComplaints() {
        List<Complaint> complaints = complaintRepository.findByComplaintStatusOrComplaintStatusOrComplaintStatus(ComplaintStatus.PENDING,ComplaintStatus.IN_PROGRESS,ComplaintStatus.RESOLVED);
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

//        List<Booking> bookings = bookingRepository.findByService_ServiceIdAndTourist_UserId(service.getServiceId(),tourist.getUserId());

        Booking booking = bookingRepository.findById(complaint.getBooking().getBookingId())
                .orElseThrow(()-> new ResourceNotFoundException("Booking",complaint.getBooking().getBookingId()));

        List<Complaint> noOfComplaints = complaintRepository.findByService_ServiceId(service.getServiceId());
        List<ComplaintImage> complaintImages = complaintImgRepository.findByComplaint_ComplaintId(id);


        ComplaintViewDTO complaintViewDTO = new ComplaintViewDTO();

        CategoryRequestDTO categoryRequestDTO = new CategoryRequestDTO();
        categoryRequestDTO.setCategoryName(service.getCategory().getCategoryName());

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
        complaintViewDTO.setInvestigationStartedDate(complaint.getInvestigationStartedDate());
        complaintViewDTO.setComplaintResult(complaint.getComplaintResult());
        complaintViewDTO.setRefundReason(complaint.getRefundReason());
        complaintViewDTO.setPaidAmount(booking.getPaidAmount().toString());
        complaintViewDTO.setComplaintStatus(complaint.getComplaintStatus());
//        complaintViewDTO.setBookings(bookings);
        List<String> complaintImgList =  new ArrayList<>();
        for(ComplaintImage complaintImage: complaintImages){
            complaintImgList.add(complaintImage.getImageUrl());
        }
        complaintViewDTO.setComplaintImgs(complaintImgList);

        return APIResponse.<ComplaintViewDTO>builder()
                .success(true)
                .message("View complaint Details")
                .data(complaintViewDTO)
                .build();
    }

    @Override
    @Transactional
    public APIResponse<String> handleComplaint(ComplaintHandleRequestDTO request) {
        if (request.getComplaintId() == null) {
            throw new BadRequestException("complaintId is required", "complaintId", null);
        }
        if (request.getComplaintResult() == null) {
            throw new BadRequestException("complaintResult is required", "complaintResult", null);
        }
        Complaint complaint = complaintRepository.findById(request.getComplaintId())
                .orElseThrow(() -> new ResourceNotFoundException("Complaint", request.getComplaintId()));
        if (complaint.getComplaintStatus() == ComplaintStatus.RESOLVED) {
            throw new ConflictException("Complaint " + complaint.getComplaintId() + " is already resolved",
                    "This complaint has already been resolved");
        }

        // The service always comes from the complaint itself, never from the request
        Service service = complaint.getService();
        if (request.getComplaintType() == ComplaintType.PROVIDER_FAULT
                && Boolean.TRUE.equals(request.getWarningStatus()) && service != null) {
            service.setWarnings((service.getWarnings() == null ? 0 : service.getWarnings()) + 1);
            Warning warning = new Warning();
            warning.setWarning(request.getWarningReason());
            warning.setService(service);
            warningRepository.save(warning);
        }

        ComplaintResult result = request.getComplaintResult();
        complaint.setComplaintResult(result);
        String message;
        switch (result) {
            case REFUND_FROM_PROVIDER, REFUND_FROM_COMPANY -> {
                if (request.getResolveId() != null) {
                    complaint.setComplaintResolve(complaintResolveRepository.findById(request.getResolveId())
                            .orElseThrow(() -> new ResourceNotFoundException("Complaint Resolve", request.getResolveId())));
                }
                complaintRefundService.refund(complaint, result);
                message = "Complaint resolved and refund issued";
            }
            case REJECT -> {
                if (request.getRejectId() != null) {
                    complaint.setComplaintReject(complaintRejectRepository.findById(request.getRejectId())
                            .orElseThrow(() -> new ResourceNotFoundException("Complaint Reject", request.getRejectId())));
                }
                message = "Complaint rejected";
            }
            default -> throw new BadRequestException("Unsupported complaint result: " + result, "complaintResult", null);
        }

        complaint.setComplaintStatus(ComplaintStatus.RESOLVED);
        complaintRepository.save(complaint);

        return APIResponse.<String>builder()
                .success(true)
                .message(message)
                .data("")
                .build();
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

    @Override
    public APIResponse<String> updateComplaintResult(Long id, ComplaintViewDTO complaintViewDTO) {
       Complaint complaint =  complaintRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Complaint",id));


//       complaint.setComplaintResult(complaintViewDTO.);
        complaint.setComplaintResult(complaintViewDTO.getComplaintResult());
       complaintRepository.save(complaint);
        return APIResponse.<String>builder()
                .success(true)
                .message("Successfully Updated the Complaint Result")
                .data("")
                .build();
    }

    @Override
    @Transactional
    public APIResponse<String> updateRefundStatus(Long id, ComplaintViewDTO complaintViewDTO) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint", id));

        String message = "Complaint updated";
        if (complaintViewDTO.getRefundStatus() == RefundStatus.REFUNDED) {
            if (complaint.getRefundStatus() == RefundStatus.REFUNDED) {
                message = "Complaint was already refunded";
            } else {
                ComplaintResult result = complaintViewDTO.getComplaintResult() != null
                        ? complaintViewDTO.getComplaintResult()
                        : complaint.getComplaintResult();
                if (result == null) {
                    throw new BadRequestException("Set the complaint result before issuing a refund", "complaintResult", null);
                }
                complaint.setComplaintResult(result);
                // Issues the real Stripe refund and marks the complaint REFUNDED
                complaintRefundService.refund(complaint, result);
                message = "Refund issued";
            }
        }

        complaint.setRefundReason(complaintViewDTO.getRefundReason());
        complaint.setComplaintStatus(ComplaintStatus.RESOLVED);
        complaintRepository.save(complaint);

        return APIResponse.<String>builder()
                .success(true)
                .message(message)
                .data("")
                .build();
    }

    public APIResponse<String> sendProviderFeedbackEmail(Long id,ComplaintViewDTO complaintViewDTO) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Complaint",id));
        Long userId = complaint.getTourist().getUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(()->new ResourceNotFoundException("User",userId));
        Provider provider = providerRepository.findByUserId(userId)
                .orElseThrow(()->new ResourceNotFoundException("Provider",userId));
        complaint.setAdminToProvider(complaintViewDTO.getAdminToProvider());
        complaint.setComplaintStatus(ComplaintStatus.RESOLVED);
        complaintRepository.save(complaint);

        Map<String, Object> params = new HashMap<>();
        params.put("name", provider.getBusinessName());
        params.put("adminFeedback", complaintViewDTO.getAdminToProvider()); // Change to match template
        params.put("emailContent", "You have received a complaint, please make sure below mentioned points are taken into consideration and taking measures to not to happen them again");

        emailService.sendEmail(
                user.getEmail(),
                "Complaint Feedback",
                "emails/provider-feedback", // template path relative to templates dir
                params
        );
        return APIResponse.<String>builder()
                .success(true)
                .message("Email Sent Successfully")
                .data("")
                .build();
    }
    public APIResponse<String> sendTouristFeedbackEmail(Long id,ComplaintViewDTO complaintViewDTO) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Complaint",id));
        Long userId = complaint.getTourist().getUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(()->new ResourceNotFoundException("User",userId));
        Tourist tourist= touristRepository.findByUserId(userId)
                .orElseThrow(()->new ResourceNotFoundException("Provider",userId));
        complaint.setAdminToTourist(complaintViewDTO.getAdminToTourist());
        complaint.setComplaintStatus(ComplaintStatus.RESOLVED);
        complaintRepository.save(complaint);

        Map<String, Object> params = new HashMap<>();
        params.put("name", tourist.getFirstName());
        params.put("adminFeedback", complaintViewDTO.getAdminToTourist()); // Change to match template
//        params.put("emailContent", "You have received a complaint, please make sure below mentioned points are taken into consideration and taking measures to not to happen them again");

        emailService.sendEmail(
                user.getEmail(),
                "Complaint Feedback",
                "emails/tourist-feedback", // template path relative to templates dir
                params
        );
        return APIResponse.<String>builder()
                .success(true)
                .message("Email Sent Successfully")
                .data("")
                .build();
    }


}
