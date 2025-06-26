package com.lankatrails.lankatrails_backend.service.impl;

import com.lankatrails.lankatrails_backend.dtos.request.ActivityServiceRequest;
import com.lankatrails.lankatrails_backend.dtos.request.TabSectionRequest;
import com.lankatrails.lankatrails_backend.dtos.response.ActivityServiceResponse;
import com.lankatrails.lankatrails_backend.exception.APIException;
import com.lankatrails.lankatrails_backend.exception.ResourceNotFoundException;
import com.lankatrails.lankatrails_backend.model.*;
import com.lankatrails.lankatrails_backend.repositories.*;
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



    @Override
    @Transactional
    public ActivityServiceRequest addService(ActivityService services, Long categoryId, Long providerId) {

        Category category=categoryRepository.findById(categoryId)
                .orElseThrow(()->new ResourceNotFoundException("Category",categoryId));

        services.setCategory(category);

        Provider provider=providerRepository.findById(providerId)
                .orElseThrow(()->new ResourceNotFoundException("Provider",providerId));

        services.setProvider(provider);

        Services saved_service=activityServiceRepository.save(services);
        return modelMapper.map(saved_service,ActivityServiceRequest.class);
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
  public Boolean removeTabs(Long id){
        tabsSectionRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Activity Service Tabs",id));

        tabsSectionRepository.deleteById(id);
        return true;

  }

  @Override
  @Transactional
  public ActivityServiceRequest updateWithId(Long Id,ActivityServiceRequest activityService){

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

      ActivityServiceRequest responseDTO=modelMapper.map(activityServiceRepository.findById(Id),ActivityServiceRequest.class);
      responseDTO.setTabsSection(reqTabs);
      return responseDTO;



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

}