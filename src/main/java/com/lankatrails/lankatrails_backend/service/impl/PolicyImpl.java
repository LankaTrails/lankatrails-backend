package com.lankatrails.lankatrails_backend.service.impl;

import com.lankatrails.lankatrails_backend.dtos.request.PolicySectionRequest;
import com.lankatrails.lankatrails_backend.model.ActivityService;
import com.lankatrails.lankatrails_backend.model.PolicySection;
import com.lankatrails.lankatrails_backend.repositories.PolicySectionRepository;
import com.lankatrails.lankatrails_backend.repositories.TabsSectionRepository;
import com.lankatrails.lankatrails_backend.service.Policies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PolicyImpl implements Policies {
    @Autowired
    PolicySectionRepository policySectionRepository;

    @Override
    public Boolean addPolicies(List<PolicySectionRequest> policyReq, ActivityService lastServiceAdded) {
        if (policyReq!=null){
            for (PolicySectionRequest policy:policyReq){
                PolicySection policySection=new PolicySection();
                policySection.setHeading(policy.getHeading());
                policySection.setPolicy(policy.getPolicy());
                policySection.setService(lastServiceAdded);
                policySectionRepository.save(policySection);
            }
            return true;
        }
        return false;
    }
}
