package com.lankatrails.lankatrails_backend.service;

import com.lankatrails.lankatrails_backend.dtos.request.PolicySectionRequest;
import com.lankatrails.lankatrails_backend.dtos.request.TabSectionRequest;
import com.lankatrails.lankatrails_backend.model.ActivityService;
import com.lankatrails.lankatrails_backend.model.PolicySection;
import com.lankatrails.lankatrails_backend.model.TabsSection;
import com.lankatrails.lankatrails_backend.model.Transport;

import java.util.List;
import java.util.Set;

public interface Policies {
    Boolean addPolicies(List<PolicySectionRequest> policyReq, ActivityService lastServiceAdded);
    List<PolicySectionRequest> getAllPolicies(Long Id);
    Set<PolicySection> updatePolicies(Set<PolicySection> tabs, List<PolicySectionRequest> reqTabs, Transport transport);
}
