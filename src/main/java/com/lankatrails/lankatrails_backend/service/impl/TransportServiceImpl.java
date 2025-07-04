package com.lankatrails.lankatrails_backend.service.impl;

import com.lankatrails.lankatrails_backend.dtos.request.ActivityServiceRequest;
import com.lankatrails.lankatrails_backend.dtos.request.PolicySectionRequest;
import com.lankatrails.lankatrails_backend.dtos.request.TabSectionRequest;
import com.lankatrails.lankatrails_backend.dtos.request.TransportRequestDTO;
import com.lankatrails.lankatrails_backend.dtos.response.ActivityServiceResponse;
import com.lankatrails.lankatrails_backend.dtos.response.TransportResponseDTO;
import com.lankatrails.lankatrails_backend.exception.APIException;
import com.lankatrails.lankatrails_backend.exception.ResourceNotFoundException;
import com.lankatrails.lankatrails_backend.factory.UpdateServiceFactory;
import com.lankatrails.lankatrails_backend.model.ActivityService;
import com.lankatrails.lankatrails_backend.model.PolicySection;
import com.lankatrails.lankatrails_backend.model.TabsSection;
import com.lankatrails.lankatrails_backend.model.Transport;
import com.lankatrails.lankatrails_backend.repositories.PolicySectionRepository;
import com.lankatrails.lankatrails_backend.repositories.TabsSectionRepository;
import com.lankatrails.lankatrails_backend.repositories.TransportRepository;
import com.lankatrails.lankatrails_backend.service.TransportService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class TransportServiceImpl implements TransportService {

    @Autowired
    TransportRepository transportRepository;

    @Autowired
    PolicySectionRepository policySectionRepository;

    @Autowired
    TabsImpl tabsImpl;

    @Autowired
    PolicyImpl policiesImpl;

    @Autowired
    UpdateServiceFactory updateServiceFactory;

    @Autowired
    TabsSectionRepository tabsSectionRepository;

    @Autowired
    ModelMapper modelMapper;

    @Override
    public TransportResponseDTO getAll(Integer pageNumber, Integer pageSize){
        Pageable pageDetails= PageRequest.of(pageNumber,pageSize);
        Page<Transport> transportPage=transportRepository.findAll(pageDetails);
        List<Transport> transports=transportPage.getContent();

        if (transports.isEmpty()){
            throw new APIException("No Transport Created Until Now");
        }

        List<TransportRequestDTO>  transport_DTOs=transports.stream()
                .map(transport -> modelMapper.map(transport, TransportRequestDTO.class))
                .toList();

        TransportResponseDTO transportResponseDTO=new TransportResponseDTO();

        transportResponseDTO.setContent(transport_DTOs);
        transportResponseDTO.setLastPage(transportPage.isLast());
        transportResponseDTO.setPageNumber(transportPage.getNumber());
        transportResponseDTO.setPageSize(transportPage.getSize());
        transportResponseDTO.setTotalElements(transportPage.getTotalElements());
        transportResponseDTO.setTotalPages(transportPage.getTotalPages());

        return transportResponseDTO;

    }

    @Override
    public TransportResponseDTO getById(Long Id) {
        Transport transport=transportRepository.findById(Id)
                .orElseThrow(()->new ResourceNotFoundException("Transport Service",Id));

        TransportRequestDTO mappedObj=modelMapper.map(transport,TransportRequestDTO.class);

        List<TabSectionRequest> tabs=tabsImpl.getAllTabs(Id);
        List<PolicySectionRequest> policies = policiesImpl.getAllPolicies(Id);

        mappedObj.setTabsSection(tabs);
        mappedObj.setPolicySection(policies);

        List<TransportRequestDTO> makeResponse=new ArrayList<>();
        makeResponse.add(mappedObj);

        TransportResponseDTO responseDTO=new TransportResponseDTO();
        responseDTO.setContent(makeResponse);

        return responseDTO;
    }

    @Override
    @Transactional
    public TransportResponseDTO updateTransport(Long Id, TransportRequestDTO transportRequestDTO) {
        Transport transport=transportRepository.findById(Id)
                .orElseThrow(()->new ResourceNotFoundException("Transport Service",Id));

        Transport mappedObj=modelMapper.map(transportRequestDTO,Transport.class);
        Transport updatedObj=updateServiceFactory.updateTransport(transport,mappedObj);
        transportRepository.save(updatedObj);

        //save the tabs if updated
        //get the tabs from the database
        Set<TabsSection> tabs=transport.getTabs();
        //get the tabs from the request
        List<TabSectionRequest> reqTabs=transportRequestDTO.getTabsSection();

        Set<TabsSection> updatedTabs=tabsImpl.updateTabs(tabs,reqTabs,transport);
        tabsSectionRepository.saveAll(updatedTabs);

        //update or add policies
        Set<PolicySection> policies=transport.getPolicies();
        //get the policySection from the request
        List<PolicySectionRequest> reqPolicies=transportRequestDTO.getPolicySection();
        Set<PolicySection> updatedPolicies=policiesImpl.updatePolicies(policies,reqPolicies,transport);

        policySectionRepository.saveAll(updatedPolicies);

        List<TransportRequestDTO> makeResponse=new ArrayList<>();
        makeResponse.add(transportRequestDTO);

        TransportResponseDTO responseDTO=new TransportResponseDTO();
        responseDTO.setContent(makeResponse);

        return responseDTO;


    }

    @Override
    public TransportResponseDTO addNewTransport(TransportRequestDTO transportRequestDTO) {
        return null;
    }


}
