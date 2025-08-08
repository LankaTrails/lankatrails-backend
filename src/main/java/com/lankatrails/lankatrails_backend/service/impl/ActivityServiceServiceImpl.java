package com.lankatrails.lankatrails_backend.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.lankatrails.lankatrails_backend.dtos.request.*;
import com.lankatrails.lankatrails_backend.exception.BadCredentialsException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.ActivityServiceResponse;
import com.lankatrails.lankatrails_backend.exception.ResourceNotFoundException;
import com.lankatrails.lankatrails_backend.exception.ServiceAlreadyExistsException;
import com.lankatrails.lankatrails_backend.factory.CreateServiceFactory;
import com.lankatrails.lankatrails_backend.model.ActivityCategory;
import com.lankatrails.lankatrails_backend.model.ActivityService;
import com.lankatrails.lankatrails_backend.model.Category;
import com.lankatrails.lankatrails_backend.model.Image;
import com.lankatrails.lankatrails_backend.model.PolicySection;
import com.lankatrails.lankatrails_backend.model.Provider;
import com.lankatrails.lankatrails_backend.model.TabsSection;
import com.lankatrails.lankatrails_backend.model.enums.ServiceCategory;
import com.lankatrails.lankatrails_backend.repositories.ActivityCategoryRepository;
import com.lankatrails.lankatrails_backend.repositories.ActivityServiceRepository;
import com.lankatrails.lankatrails_backend.repositories.CategoryRepository;
import com.lankatrails.lankatrails_backend.repositories.ImageRepository;
import com.lankatrails.lankatrails_backend.repositories.PolicySectionRepository;
import com.lankatrails.lankatrails_backend.repositories.ProviderRepository;
import com.lankatrails.lankatrails_backend.repositories.TabsSectionRepository;
import com.lankatrails.lankatrails_backend.security.utils.AuthUtils;
import com.lankatrails.lankatrails_backend.service.ActivityServiceService;
import com.lankatrails.lankatrails_backend.service.ImageService;
import com.lankatrails.lankatrails_backend.service.ServicesForAll;
import com.lankatrails.lankatrails_backend.service.utils.FileUploadService;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.function.Function;
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
    private CreateServiceFactory serviceFactory;

    @Autowired
    private ImageService imageService;

    @Autowired
    private TabsImpl tabsImpl;

    @Autowired
    private PolicyImpl policyImpl;

    @Autowired
    private serviceImpl serviceImpl;

    @Autowired
    private AuthUtils authUtils;

    @Autowired
    private FileUploadService fileUploadService;

    @Autowired
    private ServicesForAll servicesForAll;

    @Override
    @Transactional
    public APIResponse<String> addService(ActivityServiceRequest services, List<MultipartFile> images) {
        System.out.println("hello"+services.getAvailabilitySlots());
        Category category = categoryRepository.findByCategoryName(ServiceCategory.ACTIVITY)
                .orElseThrow(() -> new ResourceNotFoundException("Category", 4L));

        ActivityService mappedObj = modelMapper.map(services, ActivityService.class);
        mappedObj.setCategory(category);

        Provider provider = (Provider) authUtils.loggedInUser();
        mappedObj.setProvider(provider);

        mappedObj.setLocations(servicesForAll.setServiceLocation(services));

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
            List<PolicySectionRequest> policyReq = services.getPolicySection();
            policyImpl.addPolicies(policyReq, lastServiceAdded, category);

            // Upload and associate images
            imageService.uploadImagesForService(images, lastServiceAdded);

            // Set the availability slots
            List<AvailabilitySlotDTO> availabilitySlots = services.getAvailabilitySlots();
            for(AvailabilitySlotDTO availabilitySlotDTO : availabilitySlots){
                if(availabilitySlotDTO.getCloseTime().isEmpty() || availabilitySlotDTO.getOpenTime().isEmpty()){
                    throw new BadCredentialsException("Invalid Availability Slots","All Week Days should have the schedule");
                }
            }
            serviceImpl.setAvailabilitySlots(availabilitySlots,lastServiceAdded);

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
    public APIResponse<ActivityServiceResponse> getAll_ActivityServices(Integer pageNumber, Integer pageSize){

        Pageable pageDetails= PageRequest.of(pageNumber,pageSize);

        Page<ActivityService> activityServicePage=activityServiceRepository.findAll(pageDetails);

        List<ActivityService> activityServices=activityServicePage.getContent();

//        if (activityServices.isEmpty())
//            throw new APIException("No Activity Service created till now");

        List<ActivityServiceRequest> activityServices_DTOs= new ArrayList<>();

        for (ActivityService activity :activityServicePage){
            ActivityServiceRequest activityServiceRequest = new ActivityServiceRequest();
            if (activity.getStatus()){
                activityServiceRequest.setServiceId(activity.getServiceId());
                activityServiceRequest.setServiceName(activity.getServiceName());
                activityServiceRequest.setStatus(activity.getStatus());
                activityServices_DTOs.add(activityServiceRequest);
            }

        }

        ActivityServiceResponse activityServiceResponse=new ActivityServiceResponse();

        activityServiceResponse.setContent(activityServices_DTOs);
        activityServiceResponse.setLastPage(activityServicePage.isLast());
        activityServiceResponse.setPageNumber(activityServicePage.getNumber());
        activityServiceResponse.setPageSize(activityServicePage.getSize());
        activityServiceResponse.setTotalElements(activityServicePage.getTotalElements());
        activityServiceResponse.setTotalPages(activityServicePage.getTotalPages());
        return  APIResponse.<ActivityServiceResponse>builder()
                .success(true)
                .message("Activity Services Fetched")
                .data(activityServiceResponse)
                .build();
    }

  @Override
  @Transactional
  public APIResponse<ActivityServiceRequest> searchWithId(Long Id){
        ActivityService activityService=activityServiceRepository.findById(Id)
                .orElseThrow(()->new ResourceNotFoundException("Activity Service",Id));

        List<TabsSection> tabsSection=tabsSectionRepository.findByService_ServiceId(Id);
        List<TabSectionRequest> tabs=new ArrayList<>();

        for (TabsSection tab :tabsSection){
            TabSectionRequest tabReq = new TabSectionRequest();
            tabReq.setId(tab.getId());
            tabReq.setHeading(tab.getHeading());
            tabReq.setContent(tab.getContent());
            tabs.add(tabReq);
        }

        List<PolicySection> policySection = policySectionRepository.findByProviderIdAndCategoryIdOrNull(authUtils.loggedInUserId(),4L);

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
        prepareResponse.setContactNo(activityService.getContactNo());
        prepareResponse.setServiceId(Id);
        prepareResponse.setTabsSection(tabs);
        prepareResponse.setPolicySection(policies);
        prepareResponse.setImages(imgDTOs);

        return  APIResponse.<ActivityServiceRequest>builder()
                .success(true)
                .message("Fetched Activity Service ")
                .data(prepareResponse)
                .build();

  }
  @Override
  public APIResponse<ActivityServiceRequest> removeActivityService(Long Id){
        ActivityService activity=activityServiceRepository.findById(Id)
                .orElseThrow(()->new ResourceNotFoundException("Activity Service",Id));
        activity.setStatus(false);
        ActivityService activityService=activityServiceRepository.save(activity);

        ActivityServiceRequest activityServiceResponse=new ActivityServiceRequest();
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
  public APIResponse<String> removeTabs(Long id){
        tabsSectionRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Activity Service Tabs",id));

        tabsSectionRepository.deleteById(id);
        return  APIResponse.<String>builder()
                .success(true)
                .message("Tab Deleted Successfully")
                .data("")
                .build();

  }

  @Override
  @Transactional
  public APIResponse<String> addNewPolicy(Long id,PolicySection policies) {
        ActivityService service=activityServiceRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Activity Service",id));

        //check whether the policy exists
        PolicySection policyCheck = policySectionRepository.findByHeading(policies.getHeading());
        if (policyCheck==null){
            //Policy doesn't exist
            policies.setProvider((Provider) authUtils.loggedInUser());
            policies.getServices().add(service);
            service.getPolicies().add(policies);
            policySectionRepository.save(policies);

            return APIResponse.<String>builder()
                    .success(true)
                    .message("Policy Added Successfully")
                    .data("")
                    .build();
        }else{
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
        //check whether the policy exists
        PolicySection policyCheck = policySectionRepository.findByHeading(policies.getHeading());
        if (policyCheck==null){
            //Policy doesn't exist
            policies.setProvider((Provider) authUtils.loggedInUser());
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
  public APIResponse<String> updateWithId(Long Id,ActivityServiceRequest activityService){

      ActivityService activity=activityServiceRepository.findById(Id)
              .orElseThrow(()->new ResourceNotFoundException("Activity Service",Id));


      //update the activity service
      activity.setServiceName(activityService.getServiceName());
      activity.setContactNo(activityService.getContactNo());
      activity.setStatus(activityService.getStatus());
      activity.setActivityDetails(activityService.getActivityDetails());
      activity.setSafetyInstructions(activityService.getSafetyInstructions());
      activity.setPrice(activityService.getPrice());
      activity.setPriceType(activityService.getPriceType());
      
      // Update locations
      if (activityService.getLocations() != null && !activityService.getLocations().isEmpty()) {
          activity.setLocations(servicesForAll.setServiceLocation(activityService));
      }

      //save the updated activity service
      activityServiceRepository.save(activity);

      //update or add tabs
      //get the tabs from the database
      Set<TabsSection> tabs=activity.getTabs();

      //get the tabs from the request
      List<TabSectionRequest> reqTabs=activityService.getTabsSection();

      //create a map of existing tabs by ID for quick lookup
      Map<Long,TabsSection> savedTabMap=tabs.stream()
              .collect(Collectors.toMap(TabsSection::getId, Function.identity()));

      //create a set to track updated or newly added tabs
      Set<TabsSection> updatedTabs=new HashSet<>();

      for (TabSectionRequest req:reqTabs){
          TabsSection tab;
          if (req.getId()!=null && savedTabMap.containsKey(req.getId())){
              //update the existing tab
              tab=savedTabMap.get(req.getId());
              tab.setHeading(req.getHeading());
              tab.setContent(req.getContent());
          }else{
              //create new tab
              tab=new TabsSection();
              tab.setHeading(req.getHeading());
              tab.setContent(req.getContent());
              tab.setService(activity);
          }
          updatedTabs.add(tab);
      }

      tabsSectionRepository.saveAll(updatedTabs);

      //update or add policies
      Set<PolicySection> policies=activity.getPolicies();

      //get the policySection from the request
      List<PolicySectionRequest> reqPolicies=activityService.getPolicySection();
      //create a map from existing policy ids in the db for easy lookup
      Map<Long,PolicySection> savedPoliciesMap=policies.stream()
              .collect(Collectors.toMap(PolicySection::getId,Function.identity()));

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
              policySection.setProvider(activity.getProvider());
          }
          updatedPolicies.add(policySection);

      }
      policySectionRepository.saveAll(updatedPolicies);

      return APIResponse.<String>builder()
              .success(true)
              .message("Updated Successfully")
              .data("")
              .build();



  }

    @Override
    public ActivityServiceRequest addTabs(Long Id, TabsSection tabsSection) {
        ActivityService service=activityServiceRepository.findById(Id)
                .orElseThrow(()->new ResourceNotFoundException("Activity Service",Id));

        tabsSection.setService(service);
        tabsSectionRepository.save(tabsSection);

        List<TabsSection> tabs=tabsSectionRepository.findByService_ServiceId(Id);
        List<TabSectionRequest> tabResponse=new ArrayList<>();
        for (TabsSection tab :tabs){
            TabSectionRequest tabSectionRequest=new TabSectionRequest();
            tabSectionRequest.setId(tab.getId());
            tabSectionRequest.setHeading(tab.getHeading());
            tabSectionRequest.setContent(tab.getContent());
            tabResponse.add(tabSectionRequest);
        }

        ActivityServiceRequest responseDTO=modelMapper.map(service,ActivityServiceRequest.class);

        responseDTO.setTabsSection(tabResponse);
        return responseDTO;


    }

    @Override
    public APIResponse<String> removePolicies(Long id){
        policySectionRepository.findById(id)
                        .orElseThrow(()->new ResourceNotFoundException("Activity Service Policy",id));
        policySectionRepository.deleteById(id);
        return APIResponse.<String>builder()
                .success(true)
                .message("Policy removed successfully")
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