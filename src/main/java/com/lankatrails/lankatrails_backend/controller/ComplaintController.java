package com.lankatrails.lankatrails_backend.controller;

import com.lankatrails.lankatrails_backend.dtos.request.ComplaintDTO;
import com.lankatrails.lankatrails_backend.dtos.request.ComplaintHandleRequestDTO;
import com.lankatrails.lankatrails_backend.dtos.request.ComplaintViewDTO;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.ComplaintInfoResponse;
import com.lankatrails.lankatrails_backend.dtos.response.ComplaintViewResponse;
import com.lankatrails.lankatrails_backend.service.ComplaintService;
import com.lankatrails.lankatrails_backend.service.utils.FileUploadService;
import com.lankatrails.lankatrails_backend.model.enums.UploadCategory;
import io.vavr.API;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.ArrayList;

@RestController
@RequestMapping("/api")
public class ComplaintController {
    @Autowired
    ComplaintService complaintService;
    
    @Autowired
    FileUploadService fileUploadService;

    //Add a new complaint by the tourist
    @PostMapping("/tourist/make-complaint/{serviceId}")
    public ResponseEntity<APIResponse<String>> makeComplaint(ComplaintDTO complaintDTO){
        APIResponse<String> response = complaintService.addNewComplaint(complaintDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    //Add a new general complaint by the tourist (not service-specific)
    @PostMapping("/tourist/make-general-complaint")
    public ResponseEntity<APIResponse<String>> makeGeneralComplaint(@RequestBody ComplaintDTO complaintDTO){
        APIResponse<String> response = complaintService.addNewGeneralComplaint(complaintDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    //Upload images for complaints
    @PostMapping("/tourist/upload-complaint-images")
    public ResponseEntity<APIResponse<List<String>>> uploadComplaintImages(
            HttpServletRequest request) {
        
        System.out.println("Upload complaint images endpoint called");
        
        try {
            // Handle multipart request manually to debug
            if (!(request instanceof MultipartHttpServletRequest)) {
                System.out.println("Request is not a multipart request");
                return ResponseEntity.badRequest()
                        .body(new APIResponse<>(false, "Request must be multipart/form-data", null));
            }
            
            MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
            List<MultipartFile> images = multipartRequest.getFiles("images");
            
            System.out.println("Number of images received: " + (images != null ? images.size() : 0));
            
            if (images == null || images.isEmpty()) {
                System.out.println("No images found in request");
                return ResponseEntity.badRequest()
                        .body(new APIResponse<>(false, "No images provided", null));
            }
            
            List<String> imageUrls = new ArrayList<>();
            
            for (MultipartFile image : images) {
                if (!image.isEmpty()) {
                    System.out.println("Processing image: " + image.getOriginalFilename() + ", size: " + image.getSize());
                    String imageUrl = fileUploadService.storeFile(image, UploadCategory.COMPLAINT_IMAGES, null);
                    imageUrls.add(imageUrl);
                    System.out.println("Image uploaded successfully: " + imageUrl);
                }
            }
            
            System.out.println("All images uploaded successfully. Total: " + imageUrls.size());
            return ResponseEntity.ok(new APIResponse<>(true, "Images uploaded successfully", imageUrls));
            
        } catch (Exception e) {
            System.out.println("Error uploading images: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(new APIResponse<>(false, "Failed to upload images: " + e.getMessage(), null));
        }
    }

    //load all the complaints in the admin panel
    @GetMapping("/admin/complaints")
    public ResponseEntity<APIResponse<ComplaintInfoResponse>> getUnresolvedComplaints(){
        APIResponse<ComplaintInfoResponse> response = complaintService.getAllComplaints();
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    //view one complaint
    @GetMapping("/admin/complaints/{complaintId}")
    public ResponseEntity<APIResponse<ComplaintViewDTO>> viewOneComplaint(@PathVariable Long complaintId){
        APIResponse<ComplaintViewDTO> response = complaintService.viewOneComplaint(complaintId);
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    //handle the complaint
    @PutMapping("/admin/complaints/")
    public ResponseEntity<APIResponse<String>> handleComplaint(@RequestBody ComplaintHandleRequestDTO complaintHandleRequestDTO){
        APIResponse<String> response = complaintService.handleComplaint(complaintHandleRequestDTO);
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    //Change to "In Progress" and add the investigation started date
    @PutMapping("/admin/complaints/{complaintId}")
    public ResponseEntity<APIResponse<String>> updateComplaintStatus(@PathVariable Long complaintId, @RequestBody ComplaintViewDTO complaintViewDTO){
        APIResponse<String> response = complaintService.updateProgress(complaintId,complaintViewDTO);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    //Update the Complaint Result
    @PutMapping("/admin/complaint-result/{complaintId}")
    public ResponseEntity<APIResponse<String>> updateComplaintResult(@PathVariable Long complaintId, @RequestBody ComplaintViewDTO complaintViewDTO){
        APIResponse<String> response = complaintService.updateComplaintResult(complaintId,complaintViewDTO);
        return  new ResponseEntity<>(response,HttpStatus.OK);
    }

    //Update the complaint
    @PutMapping("/admin/complaint-refund/{complaintId}")
    public ResponseEntity<APIResponse<String>> updateRefundStatus(@PathVariable Long complaintId, @RequestBody ComplaintViewDTO complaintViewDTO){
        APIResponse<String> response = complaintService.updateRefundStatus(complaintId,complaintViewDTO);
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    //send admin feedback email to provider
    @PutMapping("/admin/complaint-provider-feedback/{complaintId}")
    public ResponseEntity<APIResponse<String>> updateFeedbackToProvider(@PathVariable Long complaintId, @RequestBody ComplaintViewDTO complaintViewDTO){
        APIResponse<String> response = complaintService.sendProviderFeedbackEmail(complaintId,complaintViewDTO);
        return  new ResponseEntity<>(response,HttpStatus.OK);
    }

    //send admin feedback email to tourist
    @PutMapping("/admin/complaint-tourist-feedback/{complaintId}")
    public ResponseEntity<APIResponse<String>> updateFeedbackToTourist(@PathVariable Long complaintId, @RequestBody ComplaintViewDTO complaintViewDTO){
        APIResponse<String> response = complaintService.sendTouristFeedbackEmail(complaintId,complaintViewDTO);
        return  new ResponseEntity<>(response,HttpStatus.OK);
    }


}
