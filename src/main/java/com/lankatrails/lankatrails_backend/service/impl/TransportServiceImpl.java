package com.lankatrails.lankatrails_backend.service.impl;

import com.lankatrails.lankatrails_backend.dtos.request.PolicySectionRequest;
import com.lankatrails.lankatrails_backend.dtos.request.TabSectionRequest;
import com.lankatrails.lankatrails_backend.dtos.request.TransportRequestDTO;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.TransportResponseDTO;
import com.lankatrails.lankatrails_backend.exception.APIException;
import com.lankatrails.lankatrails_backend.exception.ResourceNotFoundException;
import com.lankatrails.lankatrails_backend.exception.ServiceAlreadyExistsException;
import com.lankatrails.lankatrails_backend.factory.CreateServiceFactory;
import com.lankatrails.lankatrails_backend.factory.UpdateServiceFactory;
import com.lankatrails.lankatrails_backend.model.*;
import com.lankatrails.lankatrails_backend.model.enums.ServiceCategory;
import com.lankatrails.lankatrails_backend.repositories.*;
import com.lankatrails.lankatrails_backend.security.utils.AuthUtils;
import com.lankatrails.lankatrails_backend.service.ImageService;
import com.lankatrails.lankatrails_backend.service.TransportService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class TransportServiceImpl implements TransportService {

    @Autowired
    TransportRepository transportRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    VehicleCategoryRepository vehicleCategoryRepository;
    @Autowired
    PolicySectionRepository policySectionRepository;

    @Autowired
    TabsImpl tabsImpl;

    @Autowired
    PolicyImpl policiesImpl;

    @Autowired
    ImageService imageService;

    @Autowired
    AuthUtils authUtils;

    @Autowired
    UpdateServiceFactory updateServiceFactory;

    @Autowired
    CreateServiceFactory serviceFactory;

    @Autowired
    TabsSectionRepository tabsSectionRepository;

    @Autowired
    ModelMapper modelMapper;

    @Override
    public APIResponse<TransportResponseDTO> getAll(Integer pageNumber, Integer pageSize){
        Pageable pageDetails= PageRequest.of(pageNumber,pageSize);

        Page<Transport> transportPage=transportRepository.findAll(pageDetails);

        List<Transport> transports=transportPage.getContent();

        if (transports.isEmpty()){
            throw new APIException("No Transport Created Until Now");
        }

//        List<TransportRequestDTO>  transport_DTOs=transports.stream()
//                .map(transport -> modelMapper.map(transport, TransportRequestDTO.class))
//                .toList();

        List<TransportRequestDTO> transport_DTOs = new ArrayList<>();

        for (Transport transport : transportPage){
            TransportRequestDTO transportRequestDTO = new TransportRequestDTO();
            if (transport.getStatus()){
                transportRequestDTO.setServiceId(transport.getServiceId());
                transportRequestDTO.setServiceName(transport.getServiceName());
                transportRequestDTO.setStatus(transport.getStatus());
                transport_DTOs.add(transportRequestDTO);

            }
        }
        TransportResponseDTO transportResponseDTO=new TransportResponseDTO();

        transportResponseDTO.setContent(transport_DTOs);
        transportResponseDTO.setLastPage(transportPage.isLast());
        transportResponseDTO.setPageNumber(transportPage.getNumber());
        transportResponseDTO.setPageSize(transportPage.getSize());
        transportResponseDTO.setTotalElements(transportPage.getTotalElements());
        transportResponseDTO.setTotalPages(transportPage.getTotalPages());

        return APIResponse.<TransportResponseDTO>builder()
                .success(true)
                .message("Transport Services Fetched")
                .data(transportResponseDTO)
                .build();

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
    @Transactional
    public APIResponse<String> addNewTransport(TransportRequestDTO transportRequestDTO, List<MultipartFile> images) {
        Category category=categoryRepository.findByCategoryName(ServiceCategory.TRANSPORT).orElseThrow(
                ()->new ResourceNotFoundException("Category",2L)
        );
        Transport mappedObj=modelMapper.map(transportRequestDTO,Transport.class);
        mappedObj.setCategory(category);

        Provider provider=(Provider) authUtils.loggedInUser();
        mappedObj.setProvider(provider);

        Optional<Transport> checkDb=transportRepository.findByServiceName(mappedObj.getServiceName());
        Transport lastTransportAdded;
        if (checkDb.isEmpty()){

            //set the vehicle category for the mappedObj
            VehicleCategory vehicleCategory = vehicleCategoryRepository.findByCategoryName(transportRequestDTO.getVehicleCategory());
            mappedObj.setVehicleCategory(vehicleCategory);

            // Save the base service object first
            lastTransportAdded=transportRepository.save(mappedObj);

            //set the tabs
            List<TabSectionRequest> tabsReq=transportRequestDTO.getTabsSection();
            tabsImpl.addTabs(tabsReq,lastTransportAdded);

            //set the policies
            List<PolicySectionRequest> policyReq=transportRequestDTO.getPolicySection();

            //upload and associate images
            imageService.uploadImagesForService(images,lastTransportAdded);




        }else{
            throw new ServiceAlreadyExistsException(checkDb.get().getServiceId());
        }

        return APIResponse.<String>builder()
                .success(true)
                .message("Tourist Guide Added Successfully")
                .data("")
                .build();

    }

    @Override
    public APIResponse<String> deleteTransport(Long Id) {
        Transport transport=transportRepository.findById(Id)
                .orElseThrow(()->new ResourceNotFoundException("Transport Service",Id));
        transport.setStatus(false);
        transportRepository.save(transport);
        return APIResponse.<String>builder()
                .success(true)
                .message("Transport Deleted Successfully")
                .data("")
                .build();


    }


}
