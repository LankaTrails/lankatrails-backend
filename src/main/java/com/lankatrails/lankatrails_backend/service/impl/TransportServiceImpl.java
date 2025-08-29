package com.lankatrails.lankatrails_backend.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.lankatrails.lankatrails_backend.dtos.request.*;
import com.lankatrails.lankatrails_backend.exception.BadCredentialsException;
import com.lankatrails.lankatrails_backend.model.enums.ServiceStatus;
import com.lankatrails.lankatrails_backend.repositories.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.TransportResponseDTO;
import com.lankatrails.lankatrails_backend.exception.APIException;
import com.lankatrails.lankatrails_backend.exception.ResourceNotFoundException;
import com.lankatrails.lankatrails_backend.exception.ServiceAlreadyExistsException;
import com.lankatrails.lankatrails_backend.factory.CreateServiceFactory;
import com.lankatrails.lankatrails_backend.factory.UpdateServiceFactory;
import com.lankatrails.lankatrails_backend.model.Category;
import com.lankatrails.lankatrails_backend.model.Image;
import com.lankatrails.lankatrails_backend.model.PolicySection;
import com.lankatrails.lankatrails_backend.model.Provider;
import com.lankatrails.lankatrails_backend.model.TabsSection;
import com.lankatrails.lankatrails_backend.model.Transport;
import com.lankatrails.lankatrails_backend.model.VehicleCategory;
import com.lankatrails.lankatrails_backend.model.enums.ServiceCategory;
import com.lankatrails.lankatrails_backend.security.utils.AuthUtils;
import com.lankatrails.lankatrails_backend.service.ImageService;
import com.lankatrails.lankatrails_backend.service.ServicesForAll;
import com.lankatrails.lankatrails_backend.service.TransportService;

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
    ImageRepository imageRepository;

    @Autowired
    ProviderRepository providerRepository;

    @Autowired
    ServicesForAll servicesForAll;

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
            if (transport.getStatus() == ServiceStatus.ACTIVE){
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
    @Transactional
    public APIResponse<TransportRequestDTO> getById(Long Id) {
        Transport transport=transportRepository.findById(Id)
                .orElseThrow(()->new ResourceNotFoundException("Transport Service",Id));

//        TransportRequestDTO mappedObj=modelMapper.map(transport,TransportRequestDTO.class);

        List<TabsSection> tabsSection=tabsSectionRepository.findByService_ServiceId(Id);
        List<TabSectionRequest> tabs=new ArrayList<>();

        for (TabsSection tab :tabsSection){
            TabSectionRequest tabReq = new TabSectionRequest();
            tabReq.setId(tab.getId());
            tabReq.setHeading(tab.getHeading());
            tabReq.setContent(tab.getContent());
            tabs.add(tabReq);
        }
//        List<PolicySection> policySection = policySectionRepository.findByProviderIdAndCategoryIdOrNull(authUtils.loggedInUserId(),4L);

        List<PolicySection> policySection = transport.getPolicies().stream().toList();
        List<PolicySectionRequest> policies = new ArrayList<>();
//
        for (PolicySection policy : policySection){

            PolicySectionRequest policyReq = new PolicySectionRequest();
            policyReq.setId(policy.getId());
            policyReq.setHeading(policy.getHeading());
            policyReq.setPolicy(policy.getPolicy());
            policies.add(policyReq);
        }

        //set the images
        List<Image> images = imageRepository.findByService_ServiceId(Id);
        //map images to imageDTO
        List<ImageRequestDTO> imgDTOs = new ArrayList<>();
        for (Image img : images){
            ImageRequestDTO imgDTO = new ImageRequestDTO();
            imgDTO.setId(img.getImageId());
            imgDTO.setImageUrl(img.getImageUrl());
            imgDTOs.add(imgDTO);

        }

        TransportRequestDTO prepareResponse = new TransportRequestDTO();
        prepareResponse.setServiceName(transport.getServiceName());
        prepareResponse.setVehicleCategory(transport.getVehicleCategory().getCategoryName());
        prepareResponse.setDriverIncluded(transport.getDriverIncluded());
        prepareResponse.setAirConditioned(transport.getAirConditioned());
        prepareResponse.setTransmissionType(transport.getTransmissionType());
        prepareResponse.setFuelType(transport.getFuelType());
        prepareResponse.setContactNo(transport.getContactNo());
        prepareResponse.setImages(imgDTOs);
        prepareResponse.setLocations(transport.getLocations().stream()
                .map(location -> modelMapper.map(location, LocationDTO.class))
                .collect(Collectors.toSet()));
        prepareResponse.setPolicySection(policies);
        prepareResponse.setPriceConfig(modelMapper.map(transport.getPriceConfiguration(),PriceConfigDTO.class));
        prepareResponse.setBookingConfig(modelMapper.map(transport.getBookingConfiguration(),BookingConfigDTO.class));
        prepareResponse.setServiceId(Id);
        prepareResponse.setTabsSection(tabs);




        return APIResponse.<TransportRequestDTO>builder()
                .success(true)
                .message("Fetched Transport Service")
                .data(prepareResponse)
                .build();
    }

    @Override
    @Transactional
    public TransportResponseDTO updateTransport(Long Id, TransportRequestDTO transportRequestDTO) {
        Transport transport=transportRepository.findById(Id)
                .orElseThrow(()->new ResourceNotFoundException("Transport Service",Id));

        Transport mappedObj=modelMapper.map(transportRequestDTO,Transport.class);
        Transport updatedObj=updateServiceFactory.updateTransport(transport,mappedObj);
        
        // Update basic service properties
        updatedObj.setServiceName(transportRequestDTO.getServiceName());
        updatedObj.setContactNo(transportRequestDTO.getContactNo());
        updatedObj.setStatus(transportRequestDTO.getStatus());
        updatedObj.setPriceConfiguration(modelMapper.map(transportRequestDTO.getPriceConfig(),com.lankatrails.lankatrails_backend.model.PriceConfiguration.class));
        updatedObj.setBookingConfiguration(modelMapper.map(transportRequestDTO.getBookingConfig(),com.lankatrails.lankatrails_backend.model.BookingConfiguration.class));
        
        // Update locations
        if (transportRequestDTO.getLocations() != null && !transportRequestDTO.getLocations().isEmpty()) {
            updatedObj.setLocations(servicesForAll.setServiceLocation(transportRequestDTO));
        }
        
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

        Provider provider=providerRepository.findById(authUtils.loggedInUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Provider", authUtils.loggedInUserId()));
        mappedObj.setProvider(provider);

        mappedObj.setLocations(servicesForAll.setServiceLocation(transportRequestDTO));

        Optional<Transport> checkDb=transportRepository.findByServiceName(mappedObj.getServiceName());
        Transport lastTransportAdded;
        if (checkDb.isEmpty()){

            //set the vehicle category for the mappedObj
            VehicleCategory vehicleCategory = vehicleCategoryRepository.findByCategoryName(transportRequestDTO.getVehicleCategory())
                    .orElseThrow(() -> new ResourceNotFoundException("Vehicle Category", transportRequestDTO.getVehicleCategory().name()));
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

            // Set the availability slots
            List<AvailableTimeDTO> availabilitySlots = transportRequestDTO.getAvailableTimeDTOS();
            for(AvailableTimeDTO availableTimeDTO : availabilitySlots){
                if(availableTimeDTO.getCloseTime() == null || availableTimeDTO.getOpenTime() == null){
                    throw new BadCredentialsException("Invalid Availability Slots","All Week Days should have the schedule");
                }
            }
            servicesForAll.setAvailableTime(availabilitySlots, lastTransportAdded);
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
        transport.setStatus(ServiceStatus.INACTIVE);
        transportRepository.save(transport);
        return APIResponse.<String>builder()
                .success(true)
                .message("Transport Deleted Successfully")
                .data("")
                .build();


    }

    @Override
    @Transactional
    //Adding new policy for the entire activity category
    public APIResponse<String> addNewPolicy(PolicySection policies) {
        Category category = categoryRepository.findByCategoryName(ServiceCategory.TRANSPORT)
                .orElseThrow(() -> new ResourceNotFoundException("Category", 2L));

        Provider provider = providerRepository.findById(authUtils.loggedInUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Provider", authUtils.loggedInUserId()));

        //check whether the policy exists
        PolicySection policyCheck = policySectionRepository.findByHeading(policies.getHeading());
        if (policyCheck==null){
            //Policy doesn't exist
            policies.setProvider(provider);
            policies.setCategory(category);
            policySectionRepository.save(policies);
            return APIResponse.<String>builder()
                    .success(true)
                    .message("Policy Added Successfully")
                    .data("")
                    .build();
        }else{

            return APIResponse.<String>builder()
                    .success(false)
                    .message("Policy Already Exists")
                    .data("")
                    .build();

        }

    }

    @Override
    @Transactional
    public APIResponse<String> updateTransport(Long id, TransportRequestDTO transportRequestDTO, List<MultipartFile> images) {
        Transport transport=transportRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Transport Service",id));

        VehicleCategory vehicleCategory = vehicleCategoryRepository.findByCategoryName(transportRequestDTO.getVehicleCategory())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle Category", transportRequestDTO.getVehicleCategory().name()));

        // Update basic service properties
        transport.setServiceName(transportRequestDTO.getServiceName());
        transport.setContactNo(transportRequestDTO.getContactNo());
        transport.setVehicleCategory(vehicleCategory);
        transport.setDriverIncluded(transportRequestDTO.getDriverIncluded());
        transport.setAirConditioned(transportRequestDTO.getAirConditioned());
        transport.setTransmissionType(transportRequestDTO.getTransmissionType());
        transport.setFuelType(transportRequestDTO.getFuelType());
        transport.setContactNo(transportRequestDTO.getContactNo());
        transport.setPriceConfiguration(modelMapper.map(transportRequestDTO.getPriceConfig(),com.lankatrails.lankatrails_backend.model.PriceConfiguration.class));
        transport.setBookingConfiguration(modelMapper.map(transportRequestDTO.getBookingConfig(),com.lankatrails.lankatrails_backend.model.BookingConfiguration.class));
        transport.setLocations(servicesForAll.setServiceLocation(transportRequestDTO));

        // Update the transport object in the database
        Transport updatedTransport = transportRepository.save(transport);

        // Update tabs
        tabsImpl.updateTabs(transportRequestDTO.getTabsSection(), updatedTransport);
        tabsImpl.deleteTabs(transportRequestDTO.getDeletedTabs());

        // Update policies
        policiesImpl.updatePolicies(transportRequestDTO.getPolicySection(), updatedTransport);
        policiesImpl.deletePolicies(transportRequestDTO.getDeletedPolicies(), updatedTransport);

        // Upload new images if provided
        if (images != null && !images.isEmpty()) {
            imageService.uploadImagesForService(images, updatedTransport);
        }

        // Delete images if specified
        if (transportRequestDTO.getDeletedImages() != null && !transportRequestDTO.getDeletedImages().isEmpty()) {
            imageService.deleteImages(transportRequestDTO.getDeletedImages());
        }

        return APIResponse.<String>builder()
                .success(true)
                .message("Transport Updated Successfully")
                .data("")
                .build();
    }


}
