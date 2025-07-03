package com.lankatrails.lankatrails_backend.service;

import com.lankatrails.lankatrails_backend.dtos.request.PolicySectionRequest;
import com.lankatrails.lankatrails_backend.model.ActivityService;

import java.util.List;

public interface Policies {
    Boolean addPolicies(List<PolicySectionRequest> policyReq, ActivityService lastServiceAdded);
}
