package com.lankatrails.lankatrails_backend.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.lankatrails.lankatrails_backend.dtos.request.*;
import com.lankatrails.lankatrails_backend.exception.*;
import com.lankatrails.lankatrails_backend.exception.BadRequestException;
import com.lankatrails.lankatrails_backend.model.*;
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
import com.lankatrails.lankatrails_backend.dtos.response.AccommodationResponse;
import com.lankatrails.lankatrails_backend.model.enums.ServiceCategory;
import com.lankatrails.lankatrails_backend.security.utils.AuthUtils;
import com.lankatrails.lankatrails_backend.service.AccommodationService;
import com.lankatrails.lankatrails_backend.service.ImageService;
import com.lankatrails.lankatrails_backend.service.ServicesForAll;

@Service
public class AccommodationServiceImpl implements  AccommodationService {
    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    AccommodationRepository accommodationRepository;

    @Autowired
    TabsSectionRepository tabsSectionRepository;

    @Autowired
    PolicySectionRepository policySectionRepository;

    @Autowired
    AccommodationCategoryRepository accommodationCategoryRepository;

    @Autowired
    ImageRepository imageRepository;

    @Autowired
    ProviderRepository providerRepository;

    @Autowired
    TabsImpl tabsImpl;

    @Autowired
    PolicyImpl policyImpl;

    @Autowired
    private ImageService imageService;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    AuthUtils authUtils;

    @Autowired
    ServicesForAll servicesForAll;


    @Override
    @Transactional
    public APIResponse<String> addService(AccommodationServiceRequestDTO services, List<MultipartFile> images) {
        Category category = categoryRepository.findByCategoryName(ServiceCategory.ACCOMMODATION)
                .orElseThrow(() -> new ResourceNotFoundException("Category", 4L));

        Accommodation mappedObj = modelMapper.map(services, Accommodation.class);
        mappedObj.setCategory(category);

        Provider provider = providerRepository.findById(authUtils.loggedInUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Provider", authUtils.loggedInUserId()));
        mappedObj.setProvider(provider);

        mappedObj.setLocations(servicesForAll.setServiceLocation(services));
        mappedObj.setBookingConfiguration(servicesForAll.setBookingConfig(services.getBookingConfig()));
        mappedObj.setPriceConfiguration(servicesForAll.setPriceConfig(services.getPriceConfig()));
        mappedObj.setStatus(ServiceStatus.ACTIVE);

        Optional<Accommodation> checkDb = accommodationRepository.findByServiceName(mappedObj.getServiceName());
        Accommodation lastServiceAdded;

        if (checkDb.isEmpty()) {
            AccommodationCategory accommodationCategory = accommodationCategoryRepository.findByCategoryName(services.getAccommodationType())
                    .orElseThrow(() -> new ResourceNotFoundException("Accommodation Category", String.valueOf(services.getAccommodationType())));
            mappedObj.setAccommodationCategory(accommodationCategory);

            // Save the base service object first
            lastServiceAdded = accommodationRepository.save(mappedObj);

            // Set Tabs
            List<TabSectionRequest> tabsReq = services.getTabsSection();
            tabsImpl.addTabs(tabsReq, lastServiceAdded);

            // Set Policies
            List<PolicySectionRequest> policyReq = services.getPolicySection();
            lastServiceAdded.setPolicies(policyImpl.addPolicies(policyReq, category, lastServiceAdded));

            // Upload and associate images
            if (images != null && !images.isEmpty()) {
                imageService.uploadImagesForService(images, lastServiceAdded);
            }

            // Set the availability slots
            List<AvailableTimeDTO> availabilitySlots = services.getAvailableTimeDTOS();
            if (availabilitySlots == null ){
                throw new BadRequestException("Availability Slots cannot be empty");
            }
            servicesForAll.setAvailableTime(availabilitySlots, lastServiceAdded);
            accommodationRepository.save(lastServiceAdded);

        } else {
            throw new ServiceAlreadyExistsException(checkDb.get().getServiceId());
        }

        return APIResponse.<String>builder()
                .success(true)
                .message("Service Added Successfully")
                .data("")
                .build();

    }

    @Override
    public APIResponse<AccommodationResponse> getAll_Accommodations(Integer pageNumber, Integer pageSize) {
        Pageable pageDetails= PageRequest.of(pageNumber,pageSize);

        Page<Accommodation> accommodationServicePage=accommodationRepository.findAll(pageDetails);

        List<Accommodation> accommodationServices=accommodationServicePage.getContent();

        if (accommodationServices.isEmpty())
            throw new APIException("No Activity Service created till now");

        List<AccommodationServiceRequestDTO> accommodationServices_DTOs= new ArrayList<>();

        for (Accommodation accommodation :accommodationServicePage){
            AccommodationServiceRequestDTO accommodationServiceRequest = new AccommodationServiceRequestDTO();
            if (accommodation.getStatus() == ServiceStatus.ACTIVE){
                accommodationServiceRequest.setServiceId(accommodation.getServiceId());
                accommodationServiceRequest.setServiceName(accommodation.getServiceName());
                accommodationServiceRequest.setStatus(accommodation.getStatus());
                accommodationServices_DTOs.add(accommodationServiceRequest);
            }

        }

        AccommodationResponse accommodationResponse=new AccommodationResponse();

        accommodationResponse.setContent(accommodationServices_DTOs);
        accommodationResponse.setLastPage(accommodationServicePage.isLast());
        accommodationResponse.setPageNumber(accommodationServicePage.getNumber());
        accommodationResponse.setPageSize(accommodationServicePage.getSize());
        accommodationResponse.setTotalElements(accommodationServicePage.getTotalElements());
        accommodationResponse.setTotalPages(accommodationServicePage.getTotalPages());
        return  APIResponse.<AccommodationResponse>builder()
                .success(true)
                .message("Accommodation Services Fetched")
                .data(accommodationResponse)
                .build();
    }

    @Override
    @Transactional
    public APIResponse<AccommodationServiceRequestDTO> searchWithId(Long Id) {
        Accommodation accommodation=accommodationRepository.findById(Id)
                .orElseThrow(()->new ResourceNotFoundException("Accommodation Service",Id));

        //get the related tabs
        List<TabsSection> tabsSection=tabsSectionRepository.findByService_ServiceId(Id);
        List<TabSectionRequest> tabs=new ArrayList<>();

        for (TabsSection tab :tabsSection){
            TabSectionRequest tabReq = new TabSectionRequest();
            tabReq.setId(tab.getId());
            tabReq.setHeading(tab.getHeading());
            tabReq.setContent(tab.getContent());
            tabs.add(tabReq);
        }

        //get the related policies
//        List<PolicySection> policySection = policySectionRepository.findByProviderIdAndCategoryIdOrNull(authUtils.loggedInUserId(),1L);
        List<PolicySection> policySection = accommodation.getPolicies().stream()
                .toList();

        List<PolicySectionRequest> policies = new ArrayList<>();
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

        AccommodationServiceRequestDTO prepareResponse = new AccommodationServiceRequestDTO();
        prepareResponse.setServiceId(accommodation.getServiceId());
        prepareResponse.setServiceName(accommodation.getServiceName());
        prepareResponse.setStatus(accommodation.getStatus());
        prepareResponse.setAccommodationType(accommodation.getAccommodationCategory().getCategoryName());
        prepareResponse.setPriceConfig(modelMapper.map(accommodation.getPriceConfiguration(), PriceConfigDTO.class));
        prepareResponse.setBookingConfig(modelMapper.map(accommodation.getBookingConfiguration(), BookingConfigDTO.class));
        prepareResponse.setFreeWifi(accommodation.getFreeWifi());
        prepareResponse.setParkingAvailable(accommodation.getParkingAvailable());
        prepareResponse.setBreakfastIncluded(accommodation.getBreakfastIncluded());
        prepareResponse.setAirConditioned(accommodation.getAirConditioned());
        prepareResponse.setSwimmingPool(accommodation.getSwimmingPool());
        prepareResponse.setPetFriendly(accommodation.getPetFriendly());
        prepareResponse.setLaundryService(accommodation.getLaundryService());
        prepareResponse.setRoomService(accommodation.getRoomService());
        prepareResponse.setGymAccess(accommodation.getGymAccess());
        prepareResponse.setSpaServices(accommodation.getSpaServices());
        prepareResponse.setLocations(accommodation.getLocations().stream()
                .map(location -> modelMapper.map(location, LocationDTO.class))
                .collect(Collectors.toSet()));
        prepareResponse.setContactNo(accommodation.getContactNo());
        prepareResponse.setServiceId(Id);
        prepareResponse.setTabsSection(tabs);
        prepareResponse.setPolicySection(policies);
        prepareResponse.setImages(imgDTOs);
        prepareResponse.setStatus(accommodation.getStatus());
        prepareResponse.setAvailableTimeDTOS(accommodation.getAvailableTimes().stream()
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

        return  APIResponse.<AccommodationServiceRequestDTO>builder()
                .success(true)
                .message("Fetched Accommodation Service")
                .data(prepareResponse)
                .build();


    }

    @Override
    @Transactional
    //Adding new policy for the entire activity category
    public APIResponse<String> addNewPolicy(PolicySection policies) {
        Category category = categoryRepository.findByCategoryName(ServiceCategory.ACCOMMODATION)
                .orElseThrow(() -> new ResourceNotFoundException("Category", 1L));

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
    public APIResponse<String> updateAccommodation(Long id, AccommodationServiceRequestDTO accommodationService, List<MultipartFile> images) {
        Accommodation accommodation = accommodationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Accommodation Service", accommodationService.getServiceId()));

        AccommodationCategory accommodationCategory = accommodationCategoryRepository.findByCategoryName(accommodationService.getAccommodationType())
                .orElseThrow(() -> new ResourceNotFoundException("Accommodation Category", String.valueOf(accommodationService.getAccommodationType())));

        // Update the accommodation details
        accommodation.setServiceName(accommodationService.getServiceName());
        accommodation.setContactNo(accommodationService.getContactNo());
        accommodation.setAccommodationCategory(accommodationCategory);
        accommodation.setFreeWifi(accommodationService.getFreeWifi());
        accommodation.setParkingAvailable(accommodationService.getParkingAvailable());
        accommodation.setBreakfastIncluded(accommodationService.getBreakfastIncluded());
        accommodation.setAirConditioned(accommodationService.getAirConditioned());
        accommodation.setSwimmingPool(accommodationService.getSwimmingPool());
        accommodation.setPetFriendly(accommodationService.getPetFriendly());
        accommodation.setLaundryService(accommodationService.getLaundryService());
        accommodation.setRoomService(accommodationService.getRoomService());
        accommodation.setGymAccess(accommodationService.getGymAccess());
        accommodation.setSpaServices(accommodationService.getSpaServices());

        // Update locations
        accommodation.setLocations(servicesForAll.setServiceLocation(accommodationService));
        // Update configurations
        accommodation.setBookingConfiguration(servicesForAll.setBookingConfig(accommodationService.getBookingConfig()));
        accommodation.setPriceConfiguration(servicesForAll.setPriceConfig(accommodationService.getPriceConfig()));

        // Save the updated service
        Accommodation updatedAccommodation = accommodationRepository.save(accommodation);

        // Set the availability slots
        List<AvailableTimeDTO> availabilitySlots = accommodationService.getAvailableTimeDTOS();
        if (availabilitySlots == null ){
            throw new BadRequestException("Availability Slots cannot be empty");
        }
        servicesForAll.setAvailableTime(availabilitySlots, updatedAccommodation);

        // Update tabs
        tabsImpl.updateTabs(accommodationService.getTabsSection(), updatedAccommodation);
        tabsImpl.deleteTabs(accommodationService.getDeletedTabs());

        // Update policies
        policyImpl.updatePolicies(accommodationService.getPolicySection(), updatedAccommodation);
        policyImpl.deletePolicies(accommodationService.getDeletedPolicies(), updatedAccommodation);

        // Upload and associate images if provided
        if (images != null && !images.isEmpty()) {
            imageService.uploadImagesForService(images, updatedAccommodation);
        }

        // Delete images that are marked for deletion
        if (accommodationService.getDeletedImages() != null && !accommodationService.getDeletedImages().isEmpty()) {
            imageService.deleteImages(accommodationService.getDeletedImages());
        }

        return APIResponse.<String>builder()
                .success(true)
                .message("Accommodation Updated Successfully")
                .data("")
                .build();
    }

    @Override
    public APIResponse<String> deleteService(Long Id) {
        Accommodation accommodation = accommodationRepository.findById(Id)
                .orElseThrow(() -> new ResourceNotFoundException("Accommodation Service", Id));

        accommodation.setStatus(ServiceStatus.INACTIVE); // Set status to false instead of deleting
        accommodationRepository.save(accommodation);

        return APIResponse.<String>builder()
                .success(true)
                .message("Accommodation Service Deleted Successfully")
                .data("")
                .build();
    }

}
