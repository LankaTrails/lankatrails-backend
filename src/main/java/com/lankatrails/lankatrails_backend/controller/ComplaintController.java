package com.lankatrails.lankatrails_backend.controller;

import com.lankatrails.lankatrails_backend.dtos.request.ComplaintDTO;
import com.lankatrails.lankatrails_backend.dtos.request.ComplaintHandleRequestDTO;
import com.lankatrails.lankatrails_backend.dtos.request.ComplaintViewDTO;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.ComplaintInfoResponse;
import com.lankatrails.lankatrails_backend.dtos.response.ComplaintViewResponse;
import com.lankatrails.lankatrails_backend.service.ComplaintService;
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


}
