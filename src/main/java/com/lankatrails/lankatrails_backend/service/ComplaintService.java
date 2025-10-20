package com.lankatrails.lankatrails_backend.service;

import com.lankatrails.lankatrails_backend.dtos.request.ComplaintDTO;
import com.lankatrails.lankatrails_backend.dtos.request.ComplaintHandleRequestDTO;
import com.lankatrails.lankatrails_backend.dtos.request.ComplaintViewDTO;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.ComplaintInfoResponse;
import com.lankatrails.lankatrails_backend.dtos.response.ComplaintViewResponse;
import com.lankatrails.lankatrails_backend.model.User;

import java.util.List;

public interface ComplaintService {
    APIResponse<String> addNewComplaint(ComplaintDTO complaintDTO);
    APIResponse<String> addNewGeneralComplaint(ComplaintDTO complaintDTO);
    APIResponse<List<ComplaintViewDTO>> getMyComplaints();
    APIResponse<ComplaintInfoResponse> getAllComplaints();
    APIResponse<ComplaintViewDTO> viewOneComplaint(Long id);
    APIResponse<String> handleComplaint(ComplaintHandleRequestDTO complaintHandleRequestDTO);
    APIResponse<String> updateProgress(Long id,ComplaintViewDTO complaintViewDTO);
    APIResponse<String> updateComplaintResult(Long id, ComplaintViewDTO complaintViewDTO);
    APIResponse<String> updateRefundStatus(Long id,ComplaintViewDTO complaintViewDTO);
    APIResponse<String> sendProviderFeedbackEmail(Long id,ComplaintViewDTO complaintViewDTO);
    APIResponse<String> sendTouristFeedbackEmail(Long id,ComplaintViewDTO complaintViewDTO);

}
