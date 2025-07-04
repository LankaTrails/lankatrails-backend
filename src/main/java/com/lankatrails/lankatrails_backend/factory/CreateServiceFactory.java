package com.lankatrails.lankatrails_backend.factory;

import com.lankatrails.lankatrails_backend.dtos.request.ActivityServiceRequest;
import com.lankatrails.lankatrails_backend.dtos.request.PolicySectionRequest;
import com.lankatrails.lankatrails_backend.dtos.request.TabSectionRequest;
import com.lankatrails.lankatrails_backend.dtos.request.TransportRequestDTO;
import com.lankatrails.lankatrails_backend.dtos.response.ActivityServiceResponse;
import com.lankatrails.lankatrails_backend.dtos.response.TransportResponseDTO;
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
    public TransportResponseDTO createTransportResponse(
        TransportRequestDTO transportRequestDTO,
        List<TabSectionRequest> tabsReq,
        List<PolicySectionRequest> policyReq
    ){
        TransportRequestDTO prepareResponse=new TransportRequestDTO();
        prepareResponse.setServiceName(transportRequestDTO.getServiceName());
        prepareResponse.setPolicySection(transportRequestDTO.getPolicySection());
        prepareResponse.setTabsSection(transportRequestDTO.getTabsSection());
        prepareResponse.setContactNo(transportRequestDTO.getContactNo());
        prepareResponse.setLocationBased(transportRequestDTO.getLocationBased());
        prepareResponse.setVehicleQty(transportRequestDTO.getVehicleQty());
        prepareResponse.setVehicleType(transportRequestDTO.getVehicleType());

        List<TransportRequestDTO> responseList=new ArrayList<>();
        responseList.add(prepareResponse);

        TransportResponseDTO response=new TransportResponseDTO();
        response.setContent(responseList);

        return response;


    }
}
