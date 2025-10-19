package com.lankatrails.lankatrails_backend.controller;

import com.lankatrails.lankatrails_backend.dtos.request.LicenseDTO;
import com.lankatrails.lankatrails_backend.dtos.request.PolicySectionRequest;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.BusinessResponseDTO;
import com.lankatrails.lankatrails_backend.model.Provider;
import com.lankatrails.lankatrails_backend.repositories.ProviderRepository;
import com.lankatrails.lankatrails_backend.security.utils.AuthUtils;
import com.lankatrails.lankatrails_backend.service.ProviderService;
import com.lankatrails.lankatrails_backend.service.impl.PolicyImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/provider")
public class ProviderController {
    @Autowired
    PolicyImpl policyImplementation;

    @Autowired
    AuthUtils authUtils;

    @Autowired
    ProviderService providerService;

    @Autowired
    ProviderRepository providerRepository;

    @PostMapping("/add/policy")
    public ResponseEntity<APIResponse<String>> addPublicPolicy
            (@RequestBody PolicySectionRequest policyReq){
        Provider provider = providerRepository.findById(authUtils.loggedInUserId())
                .orElseThrow(() -> new RuntimeException("Provider not found with id: " + authUtils.loggedInUserId()));
        policyReq.setProvider(provider);
        APIResponse<String> response = policyImplementation.providerAddPolicies(policyReq);
        return new ResponseEntity<>(response,HttpStatus.CREATED);
    }

    @GetMapping("/policies")
    public ResponseEntity<APIResponse<List<PolicySectionRequest>>> providerPolicies (){
        List<PolicySectionRequest> policies = policyImplementation.getProviderPolicies(authUtils.loggedInUserId());
        APIResponse<List<PolicySectionRequest>> response =APIResponse.<List<PolicySectionRequest>>builder()
                .success(true)
                .message("Found Provider Policies")
                .data(policies)
                .build();

        return new ResponseEntity<>(response,HttpStatus.OK);

    }
    //Policies of the provider and also the policies common to a single service
    @GetMapping("/policies/{categoryId}")
    public ResponseEntity<APIResponse<List<PolicySectionRequest>>> providerAndServicePolicies(@PathVariable Long categoryId){
        List<PolicySectionRequest> policies = policyImplementation
                .getProviderAndServicePolicies(authUtils.loggedInUserId(), categoryId);
        APIResponse<List<PolicySectionRequest>> response =APIResponse.<List<PolicySectionRequest>>builder()
                .success(true)
                .message("Found Provider and Service Policies")
                .data(policies)
                .build();

        return new ResponseEntity<>(response,HttpStatus.OK);

    }

//    @GetMapping("/business-details")
//    public ResponseEntity<APIResponse<BusinessDetailDTO>> getBusinessDetails() {
//        APIResponse<BusinessDetailDTO> response = providerService.getBusinessDetails();
//        return ResponseEntity.status(HttpStatus.OK)
//                .body(response);
//    }
    @GetMapping("/provider-details")
    public ResponseEntity<APIResponse<BusinessResponseDTO>> getProviderDetails() {
        APIResponse<BusinessResponseDTO> response = providerService.getBusinessDetails();
        return  new ResponseEntity<>(response,HttpStatus.OK);

    }

    @GetMapping("/delete/policy")
    public ResponseEntity<APIResponse<String>> removePolicy(@PathVariable Long id){
        APIResponse<String> response= policyImplementation.removePolicies(id);
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    @PostMapping(value = "/license-renewal", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<APIResponse<String>> renewLicense(
            @RequestPart("license") @Valid List<LicenseDTO> licenseDTO,
            @RequestPart(value = "licenseFiles", required = false )List<MultipartFile> licenseFiles){

        APIResponse<String> response = providerService.licenseRenewal(licenseDTO,licenseFiles);
        return null;
    }


}
