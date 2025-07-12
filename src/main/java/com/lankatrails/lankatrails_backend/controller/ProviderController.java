package com.lankatrails.lankatrails_backend.controller;

import com.lankatrails.lankatrails_backend.dtos.request.PolicySectionRequest;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.model.Provider;
import com.lankatrails.lankatrails_backend.security.utils.AuthUtils;
import com.lankatrails.lankatrails_backend.service.impl.PolicyImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/provider")
public class ProviderController {
    @Autowired
    PolicyImpl policyImplementation;

    @Autowired
    AuthUtils authUtils;
    @PostMapping("/add/policy")
    public ResponseEntity<APIResponse<String>> addPublicPolicy
            (@RequestBody PolicySectionRequest policyReq){
        Provider provider =(Provider) authUtils.loggedInUser();
        policyReq.setProvider(provider);
        APIResponse<String> response = policyImplementation.providerAddPolicies(policyReq);
        return new ResponseEntity<>(response,HttpStatus.CREATED);
    }
}
