package com.lankatrails.lankatrails_backend.service.impl;

import com.lankatrails.lankatrails_backend.dtos.request.*;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.RateAndReviewResponse;
import com.lankatrails.lankatrails_backend.dtos.response.TransportResponseDTO;
import com.lankatrails.lankatrails_backend.exception.APIException;
import com.lankatrails.lankatrails_backend.exception.BadRequestException;
import com.lankatrails.lankatrails_backend.exception.ResourceNotFoundException;
import com.lankatrails.lankatrails_backend.exception.ServiceAlreadyExistsException;
import com.lankatrails.lankatrails_backend.factory.CreateServiceFactory;
import com.lankatrails.lankatrails_backend.factory.UpdateServiceFactory;
import com.lankatrails.lankatrails_backend.model.*;
import com.lankatrails.lankatrails_backend.model.enums.ServiceCategory;
import com.lankatrails.lankatrails_backend.model.enums.ServiceStatus;
import com.lankatrails.lankatrails_backend.repositories.*;
import com.lankatrails.lankatrails_backend.security.utils.AuthUtils;
import com.lankatrails.lankatrails_backend.service.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private BookingService bookingService;

    @Override
    public APIResponse<TransportResponseDTO> getAll(Integer pageNumber, Integer pageSize) {
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize);

        Page<Transport> transportPage = transportRepository.findAll(pageDetails);

        List<Transport> transports = transportPage.getContent();

        if (transports.isEmpty()) {
            throw new APIException("No Transport Created Until Now");
        }

//        List<TransportRequestDTO>  transport_DTOs=transports.stream()
//                .map(transport -> modelMapper.map(transport, TransportRequestDTO.class))
//                .toList();

        List<TransportRequestDTO> transport_DTOs = new ArrayList<>();

        for (Transport transport : transportPage) {
            TransportRequestDTO transportRequestDTO = new TransportRequestDTO();
            if (transport.getStatus() == ServiceStatus.ACTIVE) {
                transportRequestDTO.setServiceId(transport.getServiceId());
                transportRequestDTO.setServiceName(transport.getServiceName());
                transportRequestDTO.setStatus(transport.getStatus());
                // Safely get average rating with null check
                APIResponse<RateAndReviewResponse> ratingResponse = reviewService.getAverageRatingByServiceId(transport.getServiceId());
                Double averageRating = (ratingResponse != null && ratingResponse.getData() != null)
                        ? ratingResponse.getData().getAverageRating()
                        : 0.0;
                transportRequestDTO.setAverageRating(averageRating);
                transportRequestDTO.setTotalBookingsForPastMonth(bookingService.countBookingsForServiceInPeriod(transport.getServiceId(), LocalDateTime.now().minusMonths(1), LocalDateTime.now()));
                transport_DTOs.add(transportRequestDTO);

            }
        }
        TransportResponseDTO transportResponseDTO = new TransportResponseDTO();

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
        Transport transport = transportRepository.findById(Id)
                .orElseThrow(() -> new ResourceNotFoundException("Transport Service", Id));

//        TransportRequestDTO mappedObj=modelMapper.map(transport,TransportRequestDTO.class);

        List<TabsSection> tabsSection = tabsSectionRepository.findByService_ServiceId(Id);
        List<TabSectionRequest> tabs = new ArrayList<>();

        for (TabsSection tab : tabsSection) {
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
        for (PolicySection policy : policySection) {

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
        for (Image img : images) {
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
        prepareResponse.setPriceConfig(modelMapper.map(transport.getPriceConfiguration(), PriceConfigDTO.class));
        prepareResponse.setBookingConfig(modelMapper.map(transport.getBookingConfiguration(), BookingConfigDTO.class));
        prepareResponse.setServiceId(Id);
        prepareResponse.setTabsSection(tabs);
        prepareResponse.setStatus(transport.getStatus());
        prepareResponse.setAvailableTimeDTOS(transport.getAvailableTimes().stream()
                .map(availableTime -> {
                    AvailableTimeDTO availableTimeDTO = modelMapper.map(availableTime, AvailableTimeDTO.class);
                    List<BreakTimeDTO> breakTimeDTOS = availableTime.getBreakTimes().stream()
                            .map(breakTime -> modelMapper.map(breakTime, BreakTimeDTO.class))
                            .collect(Collectors.toList());
                    availableTimeDTO.setBreakTimes(breakTimeDTOS);
                    return availableTimeDTO;
                })
                .collect(Collectors.toList())
        );


        return APIResponse.<TransportRequestDTO>builder()
                .success(true)
                .message("Fetched Transport Service")
                .data(prepareResponse)
                .build();
    }

    @Override
    @Transactional
    public TransportResponseDTO updateTransport(Long Id, TransportRequestDTO transportRequestDTO) {
        Transport transport = transportRepository.findById(Id)
                .orElseThrow(() -> new ResourceNotFoundException("Transport Service", Id));

        Transport mappedObj = modelMapper.map(transportRequestDTO, Transport.class);

        // Update basic service properties
        mappedObj.setServiceName(transportRequestDTO.getServiceName());
        mappedObj.setContactNo(transportRequestDTO.getContactNo());
        mappedObj.setStatus(transportRequestDTO.getStatus());

        // Update locations
        if (transportRequestDTO.getLocations() != null && !transportRequestDTO.getLocations().isEmpty()) {
            mappedObj.setLocations(servicesForAll.setServiceLocation(transportRequestDTO));
        }

        // Update configurations
        mappedObj.setBookingConfiguration(servicesForAll.setBookingConfig(transportRequestDTO.getBookingConfig()));
        mappedObj.setPriceConfiguration(servicesForAll.setPriceConfig(transportRequestDTO.getPriceConfig()));

        Transport updatedTransport = transportRepository.save(mappedObj);

        // Set the availability slots
        List<AvailableTimeDTO> availabilitySlots = transportRequestDTO.getAvailableTimeDTOS();
        if (availabilitySlots == null) {
            throw new BadRequestException("Availability Slots cannot be empty");
        }
        servicesForAll.setAvailableTime(availabilitySlots, updatedTransport);

        // Update tabs
        tabsImpl.updateTabs(transportRequestDTO.getTabsSection(), updatedTransport);
        tabsImpl.deleteTabs(transportRequestDTO.getDeletedTabs());

        // Update policies
        policiesImpl.updatePolicies(transportRequestDTO.getPolicySection(), updatedTransport);
        policiesImpl.deletePolicies(transportRequestDTO.getDeletedPolicies(), updatedTransport);

        List<TransportRequestDTO> makeResponse = new ArrayList<>();
        makeResponse.add(transportRequestDTO);

        TransportResponseDTO responseDTO = new TransportResponseDTO();
        responseDTO.setContent(makeResponse);

        return responseDTO;


    }

    @Override
    @Transactional
    public APIResponse<String> addNewTransport(TransportRequestDTO transportRequestDTO, List<MultipartFile> images) {
        Category category = categoryRepository.findByCategoryName(ServiceCategory.TRANSPORT).orElseThrow(
                () -> new ResourceNotFoundException("Category", 2L)
        );
        Transport mappedObj = modelMapper.map(transportRequestDTO, Transport.class);
        mappedObj.setCategory(category);

        Provider provider = providerRepository.findById(authUtils.loggedInUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Provider", authUtils.loggedInUserId()));
        mappedObj.setProvider(provider);

        mappedObj.setLocations(servicesForAll.setServiceLocation(transportRequestDTO));
        mappedObj.setBookingConfiguration(servicesForAll.setBookingConfig(transportRequestDTO.getBookingConfig()));
        mappedObj.setPriceConfiguration(servicesForAll.setPriceConfig(transportRequestDTO.getPriceConfig()));
        mappedObj.setStatus(ServiceStatus.ACTIVE);

        Optional<Transport> checkDb = transportRepository.findByServiceName(mappedObj.getServiceName());
        Transport lastTransportAdded;
        if (checkDb.isEmpty()) {

            //set the vehicle category for the mappedObj
            VehicleCategory vehicleCategory = vehicleCategoryRepository.findByCategoryName(transportRequestDTO.getVehicleCategory())
                    .orElseThrow(() -> new ResourceNotFoundException("Vehicle Category", transportRequestDTO.getVehicleCategory().name()));
            mappedObj.setVehicleCategory(vehicleCategory);

            // Save the base service object first
            lastTransportAdded = transportRepository.save(mappedObj);

            //set the tabs
            List<TabSectionRequest> tabsReq = transportRequestDTO.getTabsSection();
            tabsImpl.addTabs(tabsReq, lastTransportAdded);

            //set the policies
            List<PolicySectionRequest> policyReq = transportRequestDTO.getPolicySection();
            lastTransportAdded.setPolicies(policiesImpl.addPolicies(policyReq, category, lastTransportAdded));

            //upload and associate images
            imageService.uploadImagesForService(images, lastTransportAdded);

            // Set the availability slots
            List<AvailableTimeDTO> availabilitySlots = transportRequestDTO.getAvailableTimeDTOS();
            if (availabilitySlots == null) {
                throw new BadRequestException("Availability Slots cannot be empty");
            }
            servicesForAll.setAvailableTime(availabilitySlots, lastTransportAdded);
            transportRepository.save(lastTransportAdded);
        } else {
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
        Transport transport = transportRepository.findById(Id)
                .orElseThrow(() -> new ResourceNotFoundException("Transport Service", Id));
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
        if (policyCheck == null) {
            //Policy doesn't exist
            policies.setProvider(provider);
            policies.setCategory(category);
            policySectionRepository.save(policies);
            return APIResponse.<String>builder()
                    .success(true)
                    .message("Policy Added Successfully")
                    .data("")
                    .build();
        } else {

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
        Transport transport = transportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transport Service", id));

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
        transport.setPriceConfiguration(modelMapper.map(transportRequestDTO.getPriceConfig(), com.lankatrails.lankatrails_backend.model.PriceConfiguration.class));
        transport.setBookingConfiguration(modelMapper.map(transportRequestDTO.getBookingConfig(), com.lankatrails.lankatrails_backend.model.BookingConfiguration.class));
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
