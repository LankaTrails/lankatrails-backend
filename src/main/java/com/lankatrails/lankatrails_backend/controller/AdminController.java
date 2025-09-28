package com.lankatrails.lankatrails_backend.controller;

import com.lankatrails.lankatrails_backend.dtos.request.AcceptRejectDTO;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.ApproveLicenseResponse;
import com.lankatrails.lankatrails_backend.dtos.response.ProviderInfoResponse;
import com.lankatrails.lankatrails_backend.dtos.response.ProviderViewInfoResponse;
import com.lankatrails.lankatrails_backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AuthService authService;

    @PutMapping("/approve-provider/{providerId}")
    public ResponseEntity<APIResponse<String>> approveProvider(@PathVariable Long providerId) {
        APIResponse<String> response = authService.approveProvider(providerId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(response);
    }

    //Get all the licenses
    @GetMapping("/approve-provider-service")
    public ResponseEntity<APIResponse<ApproveLicenseResponse>> approveProviderService() {
        APIResponse<ApproveLicenseResponse> response = authService.approveProviderService();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    //Load all the licenses
    @GetMapping("/approve-provider/service-category/{providerId}")
    public ResponseEntity<APIResponse<ProviderViewInfoResponse>> approveProviderServiceCategory(@PathVariable Long providerId) {
        APIResponse<ProviderViewInfoResponse> response = authService.loadAllRequestedProviders(providerId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    //load the basic provider details
    @GetMapping("/approve-provider/providers")
    public ResponseEntity<APIResponse<ProviderInfoResponse>> getProviderInfo() {
        APIResponse<ProviderInfoResponse> response = authService.getBasicProviderInfo();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    //Approve or Reject request
    @PutMapping("/approve-provider/providers/{providerId}")
    public ResponseEntity<APIResponse<String>> approveOrReject(@RequestBody AcceptRejectDTO acceptRejectDTO) {
        APIResponse<String> approvalStatus = authService.approveOrRejectRequest(acceptRejectDTO);
        return new ResponseEntity<>(approvalStatus, HttpStatus.OK);
    }


}