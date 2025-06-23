package com.lankatrails.lankatrails_backend.service.impl;

import com.lankatrails.lankatrails_backend.dtos.request.ActivityServiceRequest;
import com.lankatrails.lankatrails_backend.dtos.response.ActivityServiceResponse;
import com.lankatrails.lankatrails_backend.exception.APIException;
import com.lankatrails.lankatrails_backend.exception.ResourceNotFoundException;
import com.lankatrails.lankatrails_backend.model.ActivityService;
import com.lankatrails.lankatrails_backend.model.Category;
import com.lankatrails.lankatrails_backend.model.Provider;
import com.lankatrails.lankatrails_backend.model.Services;
import com.lankatrails.lankatrails_backend.repositories.ActivityServiceRepository;
import com.lankatrails.lankatrails_backend.repositories.CategoryRepository;
import com.lankatrails.lankatrails_backend.repositories.ProviderRepository;
import com.lankatrails.lankatrails_backend.service.ServicesService;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ServicesServiceImpl implements ServicesService {
    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProviderRepository providerRepository;

    @Autowired
    private ActivityServiceRepository activityServiceRepository;

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

        return modelMapper.map(activityService,ActivityServiceRequest.class);
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
  public ActivityServiceRequest updateWithId(Long Id,ActivityService activityService){
      ActivityService activity=activityServiceRepository.findById(Id)
              .orElseThrow(()->new ResourceNotFoundException("Activity Service",Id));

      //update the entry
      activity.setService_name(activityService.getService_name());
      activity.setLocation_based(activityService.getLocation_based());
      activity.setContact_no(activityService.getContact_no());
      activity.setStatus(activityService.getStatus());
      activity.setActivity_type(activityService.getActivity_type());
      activity.setActivity_details(activityService.getActivity_details());
      activity.setSafety_instructions(activityService.getSafety_instructions());

      //save the updated service
      activityServiceRepository.save(activity);

      return modelMapper.map(activityServiceRepository.findById(Id),ActivityServiceRequest.class);



  }

}