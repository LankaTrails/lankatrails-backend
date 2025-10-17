package com.lankatrails.lankatrails_backend.controller;

import com.lankatrails.lankatrails_backend.dtos.request.ComplaintDTO;
import com.lankatrails.lankatrails_backend.dtos.request.ComplaintHandleRequestDTO;
import com.lankatrails.lankatrails_backend.dtos.request.ComplaintViewDTO;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.ComplaintInfoResponse;
import com.lankatrails.lankatrails_backend.dtos.response.ComplaintViewResponse;
import com.lankatrails.lankatrails_backend.service.ComplaintService;
import io.vavr.API;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ComplaintController {
    @Autowired
    ComplaintService complaintService;

    //Add a new complaint by the tourist
    @PostMapping("/tourist/make-complaint/{serviceId}")
    public ResponseEntity<APIResponse<String>> makeComplaint(ComplaintDTO complaintDTO){
        APIResponse<String> response = complaintService.addNewComplaint(complaintDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
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
