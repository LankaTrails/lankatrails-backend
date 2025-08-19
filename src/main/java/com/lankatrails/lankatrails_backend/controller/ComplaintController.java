package com.lankatrails.lankatrails_backend.controller;

import com.lankatrails.lankatrails_backend.dtos.request.ComplaintDTO;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.ComplaintResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ComplaintController {
    @PostMapping("/tourist/make-complaint/{serviceId}")
    public ResponseEntity<APIResponse<ComplaintResponseDTO>> makeComplaint(ComplaintDTO complaintDTO){
        return null;

    }

}
