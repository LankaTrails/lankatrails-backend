package com.lankatrails.lankatrails_backend.service.impl;

import com.lankatrails.lankatrails_backend.dtos.request.ActivityServiceRequest;
import com.lankatrails.lankatrails_backend.dtos.request.PolicySectionRequest;
import com.lankatrails.lankatrails_backend.dtos.request.TabSectionRequest;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.ActivityServiceResponse;
import com.lankatrails.lankatrails_backend.exception.APIException;
import com.lankatrails.lankatrails_backend.exception.ResourceNotFoundException;
import com.lankatrails.lankatrails_backend.exception.ServiceAlreadyExistsException;
import com.lankatrails.lankatrails_backend.factory.CreateServiceFactory;
import com.lankatrails.lankatrails_backend.model.*;
import com.lankatrails.lankatrails_backend.model.enums.ServiceCategory;
import com.lankatrails.lankatrails_backend.repositories.*;
import com.lankatrails.lankatrails_backend.security.utils.AuthUtils;
import com.lankatrails.lankatrails_backend.service.ServicesService;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ServicesServiceImpl implements ServicesService {
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
    private CreateServiceFactory serviceFactory;

    @Autowired
    private TabsImpl tabsImpl;

    @Autowired
    private PolicyImpl policyImpl;

    @Autowired
    private AuthUtils authUtils;

    @Override
    @Transactional
    public ActivityServiceResponse addService(ActivityServiceRequest services) {

        Category category=categoryRepository.findByCategoryName(ServiceCategory.ACTIVITY).orElseThrow(
                ()->new ResourceNotFoundException("Category",4L)
        );
        ActivityService mappedObj=modelMapper.map(services,ActivityService.class);
        mappedObj.setCategory(category);

        Provider provider=(Provider) authUtils.loggedInUser();

        mappedObj.setProvider(provider);

        Optional<ActivityService> checkDb=activityServiceRepository.findByServiceName(mappedObj.getServiceName());

        if(checkDb.isEmpty()){
            ActivityService lastServiceAdded=activityServiceRepository.save(mappedObj);

            //set the tabs
            List<TabSectionRequest> tabsReq=services.getTabsSection();
            Boolean tabAdditionStatus=tabsImpl.addTabs(tabsReq,lastServiceAdded);

            //set the policies
            List<PolicySectionRequest> policyReq=services.getPolicySection();
            Boolean policyAdditionStatus=policyImpl.addPolicies(policyReq,lastServiceAdded);

            //Set the images
            List<Image> images=services.getImages();
            for (Image img : images ){
                img.setService(mappedObj);
            }
            imageRepository.saveAll(images);
            //set the response
            if(tabAdditionStatus && policyAdditionStatus){
                ActivityServiceResponse responseDTO=serviceFactory.createServiceResponse(services,tabsReq,policyReq);

                return responseDTO;
            }else{
                throw new APIException("Couldn't Save the Activity Service");
            }



        }else {
            throw new ServiceAlreadyExistsException(checkDb.get().getServiceId());
        }




    }

    @Override
    public ActivityServiceResponse getAll_ActivityServices(Integer pageNumber, Integer pageSize){

        Pageable pageDetails= PageRequest.of(pageNumber,pageSize);

        Page<ActivityService> activityServicePage=activityServiceRepository.findAll(pageDetails);

        List<ActivityService> activityServices=activityServicePage.getContent();

        if (activityServices.isEmpty())
            throw new APIException("No Activity Service created till now");

        List<ActivityServiceRequest> activityServices_DTOs=activityServices.stream()
                .map(activityService -> modelMapper.map(activityService,ActivityServiceRequest.class))
                .toList();

        ActivityServiceResponse activityServiceResponse=new ActivityServiceResponse();

        activityServiceResponse.setContent(activityServices_DTOs);
        activityServiceResponse.setLastPage(activityServicePage.isLast());
        activityServiceResponse.setPageNumber(activityServicePage.getNumber());
        activityServiceResponse.setPageSize(activityServicePage.getSize());
        activityServiceResponse.setTotalElements(activityServicePage.getTotalElements());
        activityServiceResponse.setTotalPages(activityServicePage.getTotalPages());
        return activityServiceResponse;
    }

  @Override
  public ActivityServiceRequest searchWithId(Long Id){
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

        ActivityServiceRequest dto=modelMapper.map(activityService,ActivityServiceRequest.class);
        dto.setTabsSection(tabs);
        return dto;
  }
  @Override
  public ActivityServiceRequest removeActivityService(Long Id,ActivityService activityService){
        ActivityService activity=activityServiceRepository.findById(Id)
                .orElseThrow(()->new ResourceNotFoundException("Activity Service",Id));
        activity.setStatus(false);

        activityServiceRepository.save(activity);

        return modelMapper.map(activityServiceRepository.findById(Id),ActivityServiceRequest.class);

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
  public ActivityServiceRequest addNewPolicy(Long id,PolicySection policies) {
        ActivityService service=activityServiceRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Activity Service",id));

        policies.setService(service);
        policySectionRepository.save(policies);

        List<TabsSection> serviceTabs=tabsSectionRepository.findByService_ServiceId(id);
        List<PolicySection> servicePolicies=policySectionRepository.findByService_ServiceId(id);

        ActivityServiceRequest responseDTO=modelMapper.map(service,ActivityServiceRequest.class);

        List<PolicySectionRequest> policiesForDTO=new ArrayList<>();
        for (PolicySection policy: servicePolicies){
            PolicySectionRequest policySectionRequest=new PolicySectionRequest();
            policySectionRequest.setId(policy.getId());
            policySectionRequest.setHeading(policy.getHeading());
            policySectionRequest.setPolicy(policy.getPolicy());
            policiesForDTO.add(policySectionRequest);
        }

        List<TabSectionRequest> tabsDTO=new ArrayList<>();
        for (TabsSection tab:serviceTabs){
            TabSectionRequest tabSectionRequest=new TabSectionRequest();
            tabSectionRequest.setId(tab.getId());
            tabSectionRequest.setHeading(tab.getHeading());
            tabSectionRequest.setContent(tab.getContent());
            tabsDTO.add(tabSectionRequest);
        }

        responseDTO.setPolicySection(policiesForDTO);
        responseDTO.setTabsSection(tabsDTO);

        return responseDTO;
  }

  @Override
  @Transactional
  public APIResponse<String> updateWithId(Long Id,ActivityServiceRequest activityService){

      ActivityService activity=activityServiceRepository.findById(Id)
              .orElseThrow(()->new ResourceNotFoundException("Activity Service",Id));


      //update the activity service
      activity.setServiceName(activityService.getServiceName());
      activity.setLocationBased(activityService.getLocationBased());
      activity.setContactNo(activityService.getContactNo());
      activity.setStatus(activityService.getStatus());
      activity.setActivityType(activityService.getActivityType());
      activity.setActivityDetails(activityService.getActivityDetails());
      activity.setSafetyInstructions(activityService.getSafetyInstructions());

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
              policySection.setService(activity);
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

}