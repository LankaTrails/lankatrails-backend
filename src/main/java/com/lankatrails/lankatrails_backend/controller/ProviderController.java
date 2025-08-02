package com.lankatrails.lankatrails_backend.controller;

import com.lankatrails.lankatrails_backend.dtos.request.PolicySectionRequest;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.BusinessDetailDTO;
import com.lankatrails.lankatrails_backend.model.Provider;
import com.lankatrails.lankatrails_backend.security.utils.AuthUtils;
import com.lankatrails.lankatrails_backend.service.ProviderService;
import com.lankatrails.lankatrails_backend.service.impl.PolicyImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/add/policy")
    public ResponseEntity<APIResponse<String>> addPublicPolicy
            (@RequestBody PolicySectionRequest policyReq){
        Provider provider =(Provider) authUtils.loggedInUser();
        policyReq.setProvider(provider);
        APIResponse<String> response = policyImplementation.providerAddPolicies(policyReq);
        return new ResponseEntity<>(response,HttpStatus.CREATED);
    }


    @GetMapping("/policies")
    public ResponseEntity<APIResponse<List<PolicySectionRequest>>> providerPolicies (){
        Provider provider = (Provider) authUtils.loggedInUser();
        List<PolicySectionRequest> policies = policyImplementation.getProviderPolicies(provider.getUserId());
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
        Provider provider = (Provider) authUtils.loggedInUser();
        List<PolicySectionRequest> policies = policyImplementation
                .getProviderAndServicePolicies(provider.getUserId(),categoryId);
        APIResponse<List<PolicySectionRequest>> response =APIResponse.<List<PolicySectionRequest>>builder()
                .success(true)
                .message("Found Provider and Service Policies")
                .data(policies)
                .build();

        return new ResponseEntity<>(response,HttpStatus.OK);

    }

    @GetMapping("/business-details")
    public ResponseEntity<APIResponse<BusinessDetailDTO>> getBusinessDetails() {
        APIResponse<BusinessDetailDTO> response = providerService.getBusinessDetails();
        return ResponseEntity.status(HttpStatus.OK)
                .body(response);
    }
}
