package com.lankatrails.lankatrails_backend.service.impl;

import com.lankatrails.lankatrails_backend.dtos.request.PolicySectionRequest;
import com.lankatrails.lankatrails_backend.dtos.request.TabSectionRequest;
import com.lankatrails.lankatrails_backend.model.ActivityService;
import com.lankatrails.lankatrails_backend.model.PolicySection;
import com.lankatrails.lankatrails_backend.model.TabsSection;
import com.lankatrails.lankatrails_backend.model.Transport;
import com.lankatrails.lankatrails_backend.repositories.PolicySectionRepository;
import com.lankatrails.lankatrails_backend.service.Policies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
                policySection.setProvider(lastServiceAdded.getProvider());
                policySectionRepository.save(policySection);
            }
            return true;
        }
        return false;
    }
    @Override
    public List<PolicySectionRequest> getAllPolicies(Long Id) {

        List<PolicySection> policiesSection=policySectionRepository.findByService_ServiceId(Id);
        List<PolicySectionRequest> policies =new ArrayList<>();

        for (PolicySection policy : policiesSection){
            PolicySectionRequest policyReq = new PolicySectionRequest();
            policyReq.setId(policy.getId());
            policyReq.setHeading(policy.getHeading());
            policyReq.setPolicy(policy.getPolicy());
            policies.add(policyReq);
        }
        return policies;
    }

    @Override
    public Set<PolicySection> updatePolicies(Set<PolicySection> policies, List<PolicySectionRequest> reqPolicies, Transport transport) {
        Map<Long,PolicySection> savedPoliciesMap=policies.stream()
                .collect(Collectors.toMap(PolicySection::getId, Function.identity()));

        //create a set to track updated policies or the newly added policies
        Set<PolicySection> updatedPolicies=new HashSet<>();

        for (PolicySectionRequest policy:reqPolicies){
            PolicySection policySection;
            if (policy.getId()!=null && savedPoliciesMap.containsKey(policy.getId())){
                //update the existing tab
                policySection=savedPoliciesMap.get(policy.getId());
                policySection.setHeading(policy.getHeading());
                policySection.setPolicy(policy.getPolicy());
            }else{
                //create new tab
                policySection=new PolicySection();
                policySection.setHeading(policy.getHeading());
                policySection.setPolicy(policy.getPolicy());
                policySection.setProvider(transport.getProvider());
            }
            updatedPolicies.add(policySection);

        }
        return updatedPolicies;
    }

    @Override
    public Boolean addPoliciesToTransport(List<PolicySectionRequest> policyReq, Transport lastTransportAdded) {
        if (policyReq!=null){
                for (PolicySectionRequest policy : policyReq){
                    PolicySection policySection=new PolicySection();
                    policySection.setHeading(policy.getHeading());
                    policySection.setPolicy(policy.getPolicy());
                    policySection.setProvider(lastTransportAdded.getProvider());
                    policySectionRepository.save(policySection);
                }
                return true;
        }else {
                return false;
        }

    }
}
