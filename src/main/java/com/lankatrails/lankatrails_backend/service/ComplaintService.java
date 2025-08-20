package com.lankatrails.lankatrails_backend.service;

import com.lankatrails.lankatrails_backend.dtos.request.ComplaintDTO;
import com.lankatrails.lankatrails_backend.dtos.request.ComplaintViewDTO;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.ComplaintInfoResponse;
import com.lankatrails.lankatrails_backend.dtos.response.ComplaintViewResponse;

public interface ComplaintService {
    APIResponse<String> addNewComplaint(ComplaintDTO complaintDTO);
    APIResponse<ComplaintInfoResponse> getAllComplaints();
    APIResponse<ComplaintViewDTO> viewOneComplaint(Long id);
}
