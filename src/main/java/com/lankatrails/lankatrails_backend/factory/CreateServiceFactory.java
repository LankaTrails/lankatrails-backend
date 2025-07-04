package com.lankatrails.lankatrails_backend.factory;

import com.lankatrails.lankatrails_backend.dtos.request.ActivityServiceRequest;
import com.lankatrails.lankatrails_backend.dtos.request.PolicySectionRequest;
import com.lankatrails.lankatrails_backend.dtos.request.TabSectionRequest;
import com.lankatrails.lankatrails_backend.dtos.response.ActivityServiceResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@AllArgsConstructor
public class CreateServiceFactory {
    public ActivityServiceResponse createServiceResponse(
            ActivityServiceRequest services,
            List<TabSectionRequest> tabsReq,
            List<PolicySectionRequest> policyReq
    ){

        ActivityServiceRequest prepareResponse=new ActivityServiceRequest();
        prepareResponse.setServiceName(services.getServiceName());
        prepareResponse.setLocationBased(services.getLocationBased());
        prepareResponse.setContactNo(services.getContactNo());
        prepareResponse.setActivityType(services.getActivityType());
        prepareResponse.setActivityDetails(services.getActivityDetails());
        prepareResponse.setSafetyInstructions(services.getSafetyInstructions());
        prepareResponse.setTabsSection(tabsReq);
        prepareResponse.setPolicySection(policyReq);

        List<ActivityServiceRequest> responseList=new ArrayList<>();
        responseList.add(prepareResponse);

        ActivityServiceResponse response=new ActivityServiceResponse();
        response.setContent(responseList);

        return response;

    }
}
