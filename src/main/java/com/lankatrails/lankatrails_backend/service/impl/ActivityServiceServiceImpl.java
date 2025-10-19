package com.lankatrails.lankatrails_backend.service.impl;

import com.lankatrails.lankatrails_backend.dtos.request.*;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.ActivityServiceResponse;
import com.lankatrails.lankatrails_backend.dtos.response.RateAndReviewResponse;
import com.lankatrails.lankatrails_backend.exception.BadRequestException;
import com.lankatrails.lankatrails_backend.exception.ResourceNotFoundException;
import com.lankatrails.lankatrails_backend.exception.ServiceAlreadyExistsException;
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
public class ActivityServiceServiceImpl implements ActivityServiceService {
    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProviderRepository providerRepository;

    @Autowired
    private ActivityServiceRepository activityServiceRepository;

    @Autowired
    private TabsSectionRepository tabsSectionRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private PolicySectionRepository policySectionRepository;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private ActivityCategoryRepository activityCategoryRepository;

    @Autowired
    private ImageService imageService;

    @Autowired
    private TabsImpl tabsImpl;

    @Autowired
    private PolicyImpl policyImpl;

    @Autowired
    private ServicesForAll serviceImpl;

    @Autowired
    private AuthUtils authUtils;

    @Autowired
    private ServicesForAll servicesForAll;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private BookingService bookingService;

    @Override
    @Transactional
    public APIResponse<String> addService(ActivityServiceRequest services, List<MultipartFile> images) {
        Category category = categoryRepository.findByCategoryName(ServiceCategory.ACTIVITY)
                .orElseThrow(() -> new ResourceNotFoundException("Category", 4L));

        ActivityService mappedObj = modelMapper.map(services, ActivityService.class);
        mappedObj.setCategory(category);

        Provider provider = providerRepository.findById(authUtils.loggedInUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Provider", authUtils.loggedInUserId()));
        mappedObj.setProvider(provider);

        mappedObj.setLocations(servicesForAll.setServiceLocation(services));
        mappedObj.setBookingConfiguration(servicesForAll.setBookingConfig(services.getBookingConfig()));
        mappedObj.setPriceConfiguration(servicesForAll.setPriceConfig(services.getPriceConfig()));
        mappedObj.setStatus(ServiceStatus.ACTIVE);

        Optional<ActivityService> checkDb = activityServiceRepository.findByServiceName(mappedObj.getServiceName());
        ActivityService lastServiceAdded;

        if (checkDb.isEmpty()) {
            ActivityCategory activityCategory = activityCategoryRepository
                    .findByCategoryName(services.getActivityType())
                    .orElseThrow(() -> new ResourceNotFoundException("Activity Category", services.getActivityType().name()));
            mappedObj.setActivityCategory(activityCategory);

            // Save the base service object first
            lastServiceAdded = activityServiceRepository.save(mappedObj);

            // Set Tabs
            List<TabSectionRequest> tabsReq = services.getTabsSection();
            tabsImpl.addTabs(tabsReq, lastServiceAdded);

            // Set Policies
            lastServiceAdded.setPolicies(policyImpl.addPolicies(services.getPolicySection(), category, lastServiceAdded));

            // Upload and associate images
            imageService.uploadImagesForService(images, lastServiceAdded);

            // Set the availability slots
            List<AvailableTimeDTO> availabilitySlots = services.getAvailableTimeDTOS();
            if (availabilitySlots == null) {
                throw new BadRequestException("Availability Slots cannot be empty");
            }
            servicesForAll.setAvailableTime(availabilitySlots, lastServiceAdded);
            activityServiceRepository.save(lastServiceAdded);

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
    @Transactional
    public APIResponse<ActivityServiceResponse> getAll_ActivityServices(Integer pageNumber, Integer pageSize) {

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize);

        List<ActivityService> activityServicePage=activityServiceRepository.findByProvider_UserId(authUtils.loggedInUserId())
                .orElseThrow(()->new ResourceNotFoundException("Activity", authUtils.loggedInUserId()));

//        List<ActivityService> activityServices=activityServicePage.getContent();

//        if (activityServices.isEmpty())
//            throw new APIException("No Activity Service created till now");

        List<ActivityServiceRequest> activityServices_DTOs = new ArrayList<>();

        for (ActivityService activity : activityServicePage) {
            ActivityServiceRequest activityServiceRequest = new ActivityServiceRequest();
            if (activity.getStatus() == ServiceStatus.ACTIVE) {
                activityServiceRequest.setServiceId(activity.getServiceId());
                activityServiceRequest.setServiceName(activity.getServiceName());
                activityServiceRequest.setStatus(activity.getStatus());
                // Safely get average rating with null check
                APIResponse<RateAndReviewResponse> ratingResponse = reviewService.getAverageRatingByServiceId(activity.getServiceId());
                Double averageRating = (ratingResponse != null && ratingResponse.getData() != null)
                        ? ratingResponse.getData().getAverageRating()
                        : 0.0;
                activityServiceRequest.setAverageRating(averageRating);
                activityServiceRequest.setTotalBookingsForPastMonth(bookingService.countBookingsForServiceInPeriod(activity.getServiceId(), LocalDateTime.now().minusMonths(1), LocalDateTime.now()));
                activityServices_DTOs.add(activityServiceRequest);
            }

        }

        ActivityServiceResponse activityServiceResponse = new ActivityServiceResponse();

        activityServiceResponse.setContent(activityServices_DTOs);
//        activityServiceResponse.setLastPage(activityServicePage.isLast());
//        activityServiceResponse.setPageNumber(activityServicePage.getNumber());
//        activityServiceResponse.setPageSize(activityServicePage.getSize());
//        activityServiceResponse.setTotalElements(activityServicePage.getTotalElements());
//        activityServiceResponse.setTotalPages(activityServicePage.getTotalPages());
        return  APIResponse.<ActivityServiceResponse>builder()
                .success(true)
                .message("Activity Services Fetched")
                .data(activityServiceResponse)
                .build();
    }

    @Override
    @Transactional
    public APIResponse<ActivityServiceRequest> searchWithId(Long Id) {
        ActivityService activityService = activityServiceRepository.findById(Id)
                .orElseThrow(() -> new ResourceNotFoundException("Activity Service", Id));

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
        List<PolicySection> policySection = activityService.getPolicies().stream().toList();
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

        ActivityServiceRequest prepareResponse = new ActivityServiceRequest();

        prepareResponse.setServiceName(activityService.getServiceName());
        prepareResponse.setActivityType(activityService.getActivityCategory().getCategoryName());
        prepareResponse.setActivityDetails(activityService.getActivityDetails());
        prepareResponse.setSafetyInstructions(activityService.getSafetyInstructions());
        prepareResponse.setLocations(activityService.getLocations().stream()
                .map(location -> modelMapper.map(location, LocationDTO.class))
                .collect(Collectors.toSet()));
        prepareResponse.setPriceConfig(modelMapper.map(activityService.getPriceConfiguration(), PriceConfigDTO.class));
        prepareResponse.setBookingConfig(modelMapper.map(activityService.getBookingConfiguration(), BookingConfigDTO.class));
        prepareResponse.setContactNo(activityService.getContactNo());
        prepareResponse.setServiceId(Id);
        prepareResponse.setTabsSection(tabs);
        prepareResponse.setPolicySection(policies);
        prepareResponse.setImages(imgDTOs);
        prepareResponse.setStatus(activityService.getStatus());
        prepareResponse.setAvailableTimeDTOS(activityService.getAvailableTimes().stream()
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

        return APIResponse.<ActivityServiceRequest>builder()
                .success(true)
                .message("Fetched Activity Service ")
                .data(prepareResponse)
                .build();

    }

    @Override
    public APIResponse<ActivityServiceRequest> removeActivityService(Long Id) {
        ActivityService activity = activityServiceRepository.findById(Id)
                .orElseThrow(() -> new ResourceNotFoundException("Activity Service", Id));
        activity.setStatus(ServiceStatus.INACTIVE);
        ActivityService activityService = activityServiceRepository.save(activity);

        ActivityServiceRequest activityServiceResponse = new ActivityServiceRequest();
        activityServiceResponse.setServiceName(activityService.getServiceName());
        activityServiceResponse.setServiceId(activityService.getServiceId());
        activityServiceResponse.setStatus(activityService.getStatus());

        return APIResponse.<ActivityServiceRequest>builder()
                .success(true)
                .message("Successfully Deleted")
                .data(activityServiceResponse)
                .build();

    }

    @Override
    public APIResponse<String> removeTabs(Long id) {
        tabsSectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Activity Service Tabs", id));

        tabsSectionRepository.deleteById(id);
        return APIResponse.<String>builder()
                .success(true)
                .message("Tab Deleted Successfully")
                .data("")
                .build();

    }

    @Override
    @Transactional
    public APIResponse<String> addNewPolicy(Long id, PolicySection policies) {
        ActivityService service = activityServiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Activity Service", id));

        Provider provider = providerRepository.findById(authUtils.loggedInUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Provider", authUtils.loggedInUserId()));

        //check whether the policy exists
        PolicySection policyCheck = policySectionRepository.findByHeading(policies.getHeading());
        if (policyCheck == null) {
            //Policy doesn't exist
            policies.setProvider(provider);
            policies.getServices().add(service);
            service.getPolicies().add(policies);
            policySectionRepository.save(policies);

            return APIResponse.<String>builder()
                    .success(true)
                    .message("Policy Added Successfully")
                    .data("")
                    .build();
        } else {
//            Set<PolicySection> policy = policyCheck.stream().collect(Collectors.toSet());
//            service.getPolicies().add(policy);
            service.getPolicies().add(policyCheck);
            policyCheck.getServices().add(service);
            activityServiceRepository.save(service);
            return APIResponse.<String>builder()
                    .success(true)
                    .message("Policy Added Successfully to the service_policy")
                    .data("")
                    .build();

        }

    }

    @Override
    @Transactional
    //Adding new policy for the entire activity category
    public APIResponse<String> addNewPolicy(PolicySection policies) {
        Category category = categoryRepository.findByCategoryName(ServiceCategory.ACTIVITY)
                .orElseThrow(() -> new ResourceNotFoundException("Category", 4L));

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
    public APIResponse<String> updateWithId(Long Id, ActivityServiceRequest activityService) {

        ActivityService activity = activityServiceRepository.findById(Id)
                .orElseThrow(() -> new ResourceNotFoundException("Activity Service", Id));


        //update the activity service
        activity.setServiceName(activityService.getServiceName());
        activity.setContactNo(activityService.getContactNo());
        activity.setStatus(activityService.getStatus());
        activity.setActivityDetails(activityService.getActivityDetails());
        activity.setSafetyInstructions(activityService.getSafetyInstructions());

        // Update locations
        activity.setLocations(servicesForAll.setServiceLocation(activityService));

        // Update configurations
        activity.setBookingConfiguration(servicesForAll.setBookingConfig(activityService.getBookingConfig()));
        activity.setPriceConfiguration(servicesForAll.setPriceConfig(activityService.getPriceConfig()));

        //save the updated activity service
        ActivityService updatedActivity = activityServiceRepository.save(activity);

        // Set the availability slots
        List<AvailableTimeDTO> availabilitySlots = activityService.getAvailableTimeDTOS();
        if (availabilitySlots == null) {
            throw new BadRequestException("Availability Slots cannot be empty");
        }
        servicesForAll.setAvailableTime(availabilitySlots, updatedActivity);

        // Update tabs
        tabsImpl.updateTabs(activityService.getTabsSection(), updatedActivity);
        tabsImpl.deleteTabs(activityService.getDeletedTabs());

        // Update policies
        policyImpl.updatePolicies(activityService.getPolicySection(), updatedActivity);
        policyImpl.deletePolicies(activityService.getDeletedPolicies(), updatedActivity);


        return APIResponse.<String>builder()
                .success(true)
                .message("Updated Successfully")
                .data("")
                .build();


    }

    @Override
    public ActivityServiceRequest addTabs(Long Id, TabsSection tabsSection) {
        ActivityService service = activityServiceRepository.findById(Id)
                .orElseThrow(() -> new ResourceNotFoundException("Activity Service", Id));

        tabsSection.setService(service);
        tabsSectionRepository.save(tabsSection);

        List<TabsSection> tabs = tabsSectionRepository.findByService_ServiceId(Id);
        List<TabSectionRequest> tabResponse = new ArrayList<>();
        for (TabsSection tab : tabs) {
            TabSectionRequest tabSectionRequest = new TabSectionRequest();
            tabSectionRequest.setId(tab.getId());
            tabSectionRequest.setHeading(tab.getHeading());
            tabSectionRequest.setContent(tab.getContent());
            tabResponse.add(tabSectionRequest);
        }

        ActivityServiceRequest responseDTO = modelMapper.map(service, ActivityServiceRequest.class);

        responseDTO.setTabsSection(tabResponse);
        return responseDTO;


    }

    @Override
    public APIResponse<String> removePolicies(Long id) {
        policySectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Activity Service Policy", id));
        policySectionRepository.deleteById(id);
        return APIResponse.<String>builder()
                .success(true)
                .message("Policy removed successfully")
                .data("")
                .build();
    }

    @Override
    @Transactional
    public APIResponse<String> updateService(Long id, ActivityServiceRequest activityService, List<MultipartFile> images) {
        ActivityService activity = activityServiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Activity Service", id));

        ActivityCategory activityCategory = activityCategoryRepository
                .findByCategoryName(activityService.getActivityType())
                .orElseThrow(() -> new ResourceNotFoundException("Activity Category", activityService.getActivityType().name()));

        // Update the existing service with new details
        activity.setServiceName(activityService.getServiceName());
        activity.setContactNo(activityService.getContactNo());
//        activity.setStatus(activityService.getStatus());
        activity.setActivityDetails(activityService.getActivityDetails());
        activity.setSafetyInstructions(activityService.getSafetyInstructions());
        activity.setPriceConfiguration(modelMapper.map(activityService.getPriceConfig(), com.lankatrails.lankatrails_backend.model.PriceConfiguration.class));
        activity.setBookingConfiguration(modelMapper.map(activityService.getBookingConfig(), com.lankatrails.lankatrails_backend.model.BookingConfiguration.class));
        activity.setActivityCategory(activityCategory);

        // Update locations
        activity.setLocations(servicesForAll.setServiceLocation(activityService));

        // Save the updated activity service
        ActivityService updatedActivity = activityServiceRepository.save(activity);

        //update or add tabs
        tabsImpl.updateTabs(activityService.getTabsSection(), updatedActivity);
        tabsImpl.deleteTabs(activityService.getDeletedTabs());

        //update or add policies
        policyImpl.updatePolicies(activityService.getPolicySection(), updatedActivity);
        policyImpl.deletePolicies(activityService.getDeletedPolicies(), updatedActivity);

        // Upload and associate images
        if (images != null && !images.isEmpty()) {
            imageService.uploadImagesForService(images, updatedActivity);
        }

        // Delete images if specified
        if (activityService.getDeletedImages() != null && !activityService.getDeletedImages().isEmpty()) {
            imageService.deleteImages(activityService.getDeletedImages());
        }

        return APIResponse.<String>builder()
                .success(true)
                .message("Activity Service Updated Successfully")
                .data("")
                .build();

    }

    @Override
    public APIResponse<String> deleteService(Long Id) {
        ActivityService activity = activityServiceRepository.findById(Id)
                .orElseThrow(() -> new ResourceNotFoundException("Activity Service", Id));

        activity.setStatus(ServiceStatus.INACTIVE);
        activityServiceRepository.save(activity);

        return APIResponse.<String>builder()
                .success(true)
                .message("Activity Service Deleted Successfully")
                .data("")
                .build();
    }

//    @Override
//    public APIResponse<List<PolicySectionRequest>> getAllPolicies(){
//        Provider provider = (Provider) authUtils.loggedInUser();
//        List<PolicySectionRequest> policies = policyImpl.getProviderPolicies(provider.getUserId());
//        APIResponse<List<PolicySectionRequest>> response =APIResponse.<List<PolicySectionRequest>>builder()
//                .success(true)
//                .message("Found Provider Policies")
//                .data(policies)
//                .build();
//
//        return new ResponseEntity<>(response, HttpStatus.OK);
//
//    }

}
