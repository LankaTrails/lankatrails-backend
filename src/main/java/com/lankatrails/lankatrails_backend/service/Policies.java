package com.lankatrails.lankatrails_backend.service;

import com.lankatrails.lankatrails_backend.dtos.request.PolicySectionRequest;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.model.Category;
import com.lankatrails.lankatrails_backend.model.PolicySection;
import com.lankatrails.lankatrails_backend.model.Service;
import com.lankatrails.lankatrails_backend.model.Transport;

import java.util.List;
import java.util.Set;

public interface Policies {
    Set<PolicySection> addPolicies(List<PolicySectionRequest> policyReq, Category category, Service service);

    List<PolicySectionRequest> getAllPolicies(Long Id);

    Set<PolicySection> updatePolicies(Set<PolicySection> tabs, List<PolicySectionRequest> reqTabs, Transport transport);

    Boolean addPoliciesToTransport(List<PolicySectionRequest> policyReq, Transport transport);

    APIResponse<String> providerAddPolicies(PolicySectionRequest policyReq);

    List<PolicySectionRequest> getProviderPolicies(Long userId);

    List<PolicySectionRequest> getProviderAndServicePolicies(Long userId, Long categoryId);

    List<PolicySectionRequest> getServicePolicies(Long userId, Long categoryId);

    APIResponse<String> removePolicies(Long id);

    void updatePolicies(List<PolicySectionRequest> policyReq, Service service);

    void deletePolicies(List<PolicySectionRequest> policyReq, Service service);
}
