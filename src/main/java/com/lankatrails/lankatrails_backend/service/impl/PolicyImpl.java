package com.lankatrails.lankatrails_backend.service.impl;

import com.lankatrails.lankatrails_backend.dtos.request.PolicySectionRequest;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.exception.PolicyExistsException;
import com.lankatrails.lankatrails_backend.exception.ResourceNotFoundException;
import com.lankatrails.lankatrails_backend.exception.UserNotFoundException;
import com.lankatrails.lankatrails_backend.model.*;
import com.lankatrails.lankatrails_backend.repositories.ActivityServiceRepository;
import com.lankatrails.lankatrails_backend.repositories.PolicySectionRepository;
import com.lankatrails.lankatrails_backend.repositories.ProviderRepository;
import com.lankatrails.lankatrails_backend.security.utils.AuthUtils;
import com.lankatrails.lankatrails_backend.service.Policies;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
public class PolicyImpl implements Policies {
    @Autowired
    PolicySectionRepository policySectionRepository;
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    AuthUtils authUtils;
    @Autowired
    ActivityServiceRepository activityServiceRepository;

    @Autowired
    ProviderRepository providerRepository;

    private Boolean status;

    @Override
    public Boolean addPolicies(List<PolicySectionRequest> policyReq,
                               ActivityService lastServiceAdded,
                               Category category) {


        //check whether the policy exists
        for (PolicySectionRequest policy : policyReq){
            PolicySection policies = modelMapper.map(policy, PolicySection.class);
            PolicySection policyCheck = policySectionRepository.findByHeading(policies.getHeading());

            Provider provider = providerRepository.findByUserId(authUtils.loggedInUserId())
                    .orElseThrow(() -> new UserNotFoundException("Provider not found for user ID: " + authUtils.loggedInUserId()));

            if (policyCheck==null){
                //Policy doesn't exist
                policies.setProvider(provider);
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
//    @Transactional
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

    @Override
    public APIResponse<String> removePolicies(Long id){
        policySectionRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Policy",id));
//        policySectionRepository.findByPolicy_PolicyId(id);
        policySectionRepository.deleteById(id);
        return APIResponse.<String>builder()
                .success(true)
                .message("Policy removed successfully")
                .data("")
                .build();
    }

    @Override
    public void updatePolicies(List<PolicySectionRequest> policyReq, Service service) {
        if (policyReq == null || policyReq.isEmpty()) {
            return;
        }

        for (PolicySectionRequest policy : policyReq) {
            if (policy.getId() != null) {
                // Update existing
                PolicySection existingPolicy = policySectionRepository.findById(policy.getId())
                        .orElseThrow(() -> new IllegalArgumentException("Policy not found with ID: " + policy.getId()));

                existingPolicy.setHeading(policy.getHeading());
                existingPolicy.setPolicy(policy.getPolicy());

                // Maintain both sides of the relationship if bidirectional
                service.getPolicies().add(existingPolicy);
                existingPolicy.getServices().add(service);

            } else {
                // Create new
                PolicySection newPolicy = new PolicySection();
                newPolicy.setHeading(policy.getHeading());
                newPolicy.setPolicy(policy.getPolicy());
                newPolicy.setCategory(service.getCategory());
                newPolicy.setProvider(service.getProvider());

                // Maintain both sides
                service.getPolicies().add(newPolicy);
                if (newPolicy.getServices() != null) {
                    newPolicy.getServices().add(service);
                }

                policySectionRepository.save(newPolicy); // must save here because it's new
            }
        }
    }

    @Override
    public void deletePolicies(List<PolicySectionRequest> policyReq, Service service) {
        if (policyReq == null || policyReq.isEmpty()) {
            return;
        }

        for (PolicySectionRequest policy : policyReq) {
            if (policy.getId() != null) {
                PolicySection existingPolicy = policySectionRepository.findById(policy.getId())
                        .orElseThrow(() -> new IllegalArgumentException("Policy not found with ID: " + policy.getId()));

                // Remove the association from both sides
                service.getPolicies().remove(existingPolicy);
                existingPolicy.getServices().remove(service);

                // Now decide whether to delete the policy entirely
                if (existingPolicy.getServices().isEmpty()) {
                    policySectionRepository.delete(existingPolicy);
                } else {
                    policySectionRepository.save(existingPolicy);
                }
            }
        }
    }

}
