package com.lankatrails.lankatrails_backend.controller;

import com.lankatrails.lankatrails_backend.dtos.request.ComplaintDTO;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.ComplaintInfoResponse;
import com.lankatrails.lankatrails_backend.dtos.response.ComplaintResponseDTO;
import com.lankatrails.lankatrails_backend.service.ComplaintService;
import io.vavr.API;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

}
