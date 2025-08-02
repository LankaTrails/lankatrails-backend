package com.lankatrails.lankatrails_backend.service;

import com.lankatrails.lankatrails_backend.dtos.request.PolicySectionRequest;
import com.lankatrails.lankatrails_backend.dtos.request.TabSectionRequest;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.model.*;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Set;

public interface Policies {
    Boolean addPolicies(List<PolicySectionRequest> policyReq, ActivityService lastServiceAdded, Category category);
    List<PolicySectionRequest> getAllPolicies(Long Id);
    Set<PolicySection> updatePolicies(Set<PolicySection> tabs, List<PolicySectionRequest> reqTabs, Transport transport);
    Boolean addPoliciesToTransport(List<PolicySectionRequest> policyReq,Transport transport);
    APIResponse<String> providerAddPolicies(PolicySectionRequest policyReq);
    List<PolicySectionRequest> getProviderPolicies(Long userId);
    List<PolicySectionRequest> getProviderAndServicePolicies(Long userId,Long categoryId);
    List<PolicySectionRequest> getServicePolicies(Long userId, Long categoryId);

}
