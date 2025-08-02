package com.lankatrails.lankatrails_backend.service.impl;

import com.lankatrails.lankatrails_backend.dtos.request.PolicySectionRequest;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.exception.PolicyExistsException;
import com.lankatrails.lankatrails_backend.model.*;
import com.lankatrails.lankatrails_backend.repositories.ActivityServiceRepository;
import com.lankatrails.lankatrails_backend.repositories.PolicySectionRepository;
import com.lankatrails.lankatrails_backend.security.utils.AuthUtils;
import com.lankatrails.lankatrails_backend.service.Policies;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PolicyImpl implements Policies {
    @Autowired
    PolicySectionRepository policySectionRepository;
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    AuthUtils authUtils;
    @Autowired
    ActivityServiceRepository activityServiceRepository;

    private Boolean status;

    @Override
    public Boolean addPolicies(List<PolicySectionRequest> policyReq,
                               ActivityService lastServiceAdded,
                               Category category) {


        //check whether the policy exists
        for (PolicySectionRequest policy : policyReq){
            PolicySection policies = modelMapper.map(policy, PolicySection.class);
            PolicySection policyCheck = policySectionRepository.findByHeading(policies.getHeading());

            if (policyCheck==null){
                //Policy doesn't exist
                policies.setProvider((Provider) authUtils.loggedInUser());
                policies.getServices().add(lastServiceAdded);
                policies.setCategory(category);
                lastServiceAdded.getPolicies().add(policies);
                policySectionRepository.save(policies);
                status =true;

            }else{
//            Set<PolicySection> policy = policyCheck.stream().collect(Collectors.toSet());
//            service.getPolicies().add(policy);
                lastServiceAdded.getPolicies().add(policyCheck);
                policyCheck.getServices().add(lastServiceAdded);
                activityServiceRepository.save(lastServiceAdded);
                status = true;

            }
        }

        status = false;
        return  status;
    }
    @Override
    public List<PolicySectionRequest> getAllPolicies(Long Id) {

        List<PolicySection> policiesSection=policySectionRepository.findByServices_ServiceId(Id);
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

    @Override
    public APIResponse<String> providerAddPolicies
            (PolicySectionRequest policyReq) {
            PolicySection mappedObj = modelMapper.map(policyReq,PolicySection.class);
            PolicySection checkDb = policySectionRepository.findByHeading(mappedObj.getHeading());
            if (checkDb == null){
                PolicySection savedObj = policySectionRepository.save(mappedObj);
                return APIResponse.<String>builder()
                        .success(true)
                        .message("Policies Added To The Provider")
                        .data("")
                        .build();
            }else{
               throw new PolicyExistsException(checkDb.getId());
            }



    }

    @Override
    public List<PolicySectionRequest> getProviderPolicies(Long userId) {
        List<PolicySection> policySection=policySectionRepository.findByProvider_UserIdAndCategoryIsNull(userId);
        List<PolicySectionRequest> response = new ArrayList<>();
        for (PolicySection policy : policySection){
            PolicySectionRequest policyReq = new PolicySectionRequest();
            policyReq.setHeading(policy.getHeading());
            policyReq.setPolicy(policy.getPolicy());
            policyReq.setId(policy.getId());
            response.add(policyReq);
        }
        return response;
    }
    @Override
    public List<PolicySectionRequest> getProviderAndServicePolicies(Long userId,Long categoryId) {
        List<PolicySection> policySection=policySectionRepository.findByProviderIdAndCategoryIdOrNull(userId,categoryId);
        List<PolicySectionRequest> response = new ArrayList<>();
        for (PolicySection policy : policySection){
            PolicySectionRequest policyReq = new PolicySectionRequest();
            policyReq.setHeading(policy.getHeading());
            policyReq.setPolicy(policy.getPolicy());
            policyReq.setId(policy.getId());
            response.add(policyReq);
        }
        return response;
    }
    @Override
    public List<PolicySectionRequest> getServicePolicies(Long userId,Long categoryId) {
        List<PolicySection> policySection=policySectionRepository.findByProvider_UserIdAndCategory_CategoryId(userId,categoryId);
        List<PolicySectionRequest> response = new ArrayList<>();
        for (PolicySection policy : policySection){
            PolicySectionRequest policyReq = new PolicySectionRequest();
            policyReq.setHeading(policy.getHeading());
            policyReq.setPolicy(policy.getPolicy());
            policyReq.setId(policy.getId());
            response.add(policyReq);
        }
        return response;
    }


}
