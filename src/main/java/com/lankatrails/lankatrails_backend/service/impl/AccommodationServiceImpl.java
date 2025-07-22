package com.lankatrails.lankatrails_backend.service.impl;

import com.lankatrails.lankatrails_backend.dtos.request.*;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.AccommodationResponse;
import com.lankatrails.lankatrails_backend.dtos.response.ActivityServiceResponse;
import com.lankatrails.lankatrails_backend.exception.APIException;
import com.lankatrails.lankatrails_backend.exception.ResourceNotFoundException;
import com.lankatrails.lankatrails_backend.exception.ServiceAlreadyExistsException;
import com.lankatrails.lankatrails_backend.model.*;
import com.lankatrails.lankatrails_backend.model.enums.ServiceCategory;
import com.lankatrails.lankatrails_backend.repositories.*;
import com.lankatrails.lankatrails_backend.security.utils.AuthUtils;
import com.lankatrails.lankatrails_backend.service.AccommodationService;
import com.lankatrails.lankatrails_backend.service.ImageService;
import com.lankatrails.lankatrails_backend.service.ServicesForAll;
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
    TabsSectionRepository tabsSectionRepository;

    @Autowired
    PolicySectionRepository policySectionRepository;

    @Autowired
    AccommodationCategoryRepository accommodationCategoryRepository;

    @Autowired
    ImageRepository imageRepository;

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

        Provider provider = (Provider) authUtils.loggedInUser();
        mappedObj.setProvider(provider);

        mappedObj.setLocationBased(servicesForAll.setServiceLocation(services));

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
        List<PolicySection> policySection = policySectionRepository.findByProviderIdAndCategoryIdOrNull(authUtils.loggedInUserId(),1L);

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
            imgDTO.setImageUrl(img.getImageUrl());
            imgDTOs.add(imgDTO);

        }

        AccommodationServiceRequestDTO prepareResponse = new AccommodationServiceRequestDTO();
        prepareResponse.setServiceId(accommodation.getServiceId());
        prepareResponse.setServiceName(accommodation.getServiceName());
        prepareResponse.setStatus(accommodation.getStatus());
        prepareResponse.setAccommodationType(accommodation.getAccommodationCategory().getCategoryName());
        prepareResponse.setMaxGuests(accommodation.getMaxGuests());
        prepareResponse.setNumberOfRooms(accommodation.getNumberOfRooms());
        prepareResponse.setPrice(accommodation.getPrice());
        prepareResponse.setPriceType(accommodation.getPriceType());
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
        prepareResponse.setLocationBased(modelMapper.map(accommodation.getLocationBased(), LocationDTO.class));
        prepareResponse.setContactNo(accommodation.getContactNo());
        prepareResponse.setNumberOfRooms(accommodation.getNumberOfRooms());
        prepareResponse.setPrice(accommodation.getPrice());
        prepareResponse.setServiceId(Id);
        prepareResponse.setTabsSection(tabs);
        prepareResponse.setPolicySection(policies);
        prepareResponse.setImages(imgDTOs);

        return  APIResponse.<AccommodationServiceRequestDTO>builder()
                .success(true)
                .message("Fetched Accommodation Service")
                .data(prepareResponse)
                .build();


    }
}
