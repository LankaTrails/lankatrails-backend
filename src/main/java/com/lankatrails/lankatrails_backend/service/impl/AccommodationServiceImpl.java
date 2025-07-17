package com.lankatrails.lankatrails_backend.service.impl;

import com.lankatrails.lankatrails_backend.dtos.request.AccommodationServiceRequestDTO;
import com.lankatrails.lankatrails_backend.dtos.request.ActivityServiceRequest;
import com.lankatrails.lankatrails_backend.dtos.request.PolicySectionRequest;
import com.lankatrails.lankatrails_backend.dtos.request.TabSectionRequest;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.AccommodationResponse;
import com.lankatrails.lankatrails_backend.dtos.response.ActivityServiceResponse;
import com.lankatrails.lankatrails_backend.exception.APIException;
import com.lankatrails.lankatrails_backend.exception.ResourceNotFoundException;
import com.lankatrails.lankatrails_backend.exception.ServiceAlreadyExistsException;
import com.lankatrails.lankatrails_backend.model.Accommodation;
import com.lankatrails.lankatrails_backend.model.ActivityService;
import com.lankatrails.lankatrails_backend.model.Category;
import com.lankatrails.lankatrails_backend.model.Provider;
import com.lankatrails.lankatrails_backend.model.enums.ServiceCategory;
import com.lankatrails.lankatrails_backend.repositories.AccommodationRepository;
import com.lankatrails.lankatrails_backend.repositories.CategoryRepository;
import com.lankatrails.lankatrails_backend.security.utils.AuthUtils;
import com.lankatrails.lankatrails_backend.service.AccommodationService;
import com.lankatrails.lankatrails_backend.service.ImageService;
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

@Service
public class AccommodationServiceImpl implements  AccommodationService {
    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    AccommodationRepository accommodationRepository;

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



    @Override
    @Transactional
    public APIResponse<String> addService(AccommodationServiceRequestDTO services, List<MultipartFile> images) {
        Category category = categoryRepository.findByCategoryName(ServiceCategory.ACCOMMODATION)
                .orElseThrow(() -> new ResourceNotFoundException("Category", 4L));

        Accommodation mappedObj = modelMapper.map(services, Accommodation.class);
        mappedObj.setCategory(category);

        Provider provider = (Provider) authUtils.loggedInUser();
        mappedObj.setProvider(provider);

        Optional<Accommodation> checkDb = accommodationRepository.findByServiceName(mappedObj.getServiceName());
        Accommodation lastServiceAdded;

        if (checkDb.isEmpty()) {
            // Save the base service object first
            lastServiceAdded = accommodationRepository.save(mappedObj);

            // Set Tabs
            List<TabSectionRequest> tabsReq = services.getTabsSection();
            tabsImpl.addTabs(tabsReq, lastServiceAdded);

            // Set Policies
            List<PolicySectionRequest> policyReq = services.getPolicySection();
//            Boolean policyAdditionStatus = policyImpl.addPolicies(policyReq, lastServiceAdded, category);

            // Upload and associate images
            imageService.uploadImagesForService(images, lastServiceAdded);

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
            if (accommodation.getStatus()){
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
}
