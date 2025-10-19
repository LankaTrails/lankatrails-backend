package com.lankatrails.lankatrails_backend.service.impl;

import com.lankatrails.lankatrails_backend.dtos.request.*;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.FoodBeverageResponse;
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
import com.lankatrails.lankatrails_backend.service.utils.FileUploadService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
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
public class FoodBeverageServiceImpl implements FoodBeverageService {
    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProviderRepository providerRepository;

    @Autowired
    private FoodBeverageRepository foodBeverageRepository;

    @Autowired
    private FoodAndBeverageCategoryRepository foodAndBeverageCategoryRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private ImageService imageService;

    @Autowired
    private AuthUtils authUtils;

    @Autowired
    private FileUploadService fileUploadService;

    @Autowired
    private TabsImpl tabsImpl;

    @Autowired
    private PolicyImpl policyImpl;

    @Autowired
    private PolicySectionRepository policySectionRepository;

    @Autowired
    private TabsSectionRepository tabsSectionRepository;

    @Autowired
    private ServicesForAll servicesForAll;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private BookingService bookingService;


    @Override
    @Transactional
    public APIResponse<String> addService(FoodBeverageRequest foodBeverageRequest, List<MultipartFile> images) {
        Category category = categoryRepository.findByCategoryName(ServiceCategory.FOOD_BEVERAGE)
                .orElseThrow(() -> new ResourceNotFoundException("Category", 4L));

        FoodAndBeverage mappedObj = modelMapper.map(foodBeverageRequest, FoodAndBeverage.class);
        mappedObj.setCategory(category);

        Provider provider = providerRepository.findById(authUtils.loggedInUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Provider", authUtils.loggedInUserId()));
        mappedObj.setProvider(provider);

        mappedObj.setLocations(servicesForAll.setServiceLocation(foodBeverageRequest));
        mappedObj.setBookingConfiguration(servicesForAll.setBookingConfig(foodBeverageRequest.getBookingConfig()));
        mappedObj.setPriceConfiguration(servicesForAll.setPriceConfig(foodBeverageRequest.getPriceConfig()));
        mappedObj.setStatus(ServiceStatus.ACTIVE);

        Optional<FoodAndBeverage> checkDb = foodBeverageRepository.findByServiceName(mappedObj.getServiceName());
        FoodAndBeverage lastServiceAdded;

        if (checkDb.isEmpty()) {
            FoodAndBeverageCategory foodAndBeverageCategory = foodAndBeverageCategoryRepository
                    .findByCategoryName(foodBeverageRequest.getFoodAndBeverageType())
                    .orElseThrow(() -> new ResourceNotFoundException("Food and Beverage Category", foodBeverageRequest.getFoodAndBeverageType().getDisplayName()));
            mappedObj.setFoodAndBeverageCategory(foodAndBeverageCategory);

            // Save the base service object first
            lastServiceAdded = foodBeverageRepository.save(mappedObj);

            // Set Tabs
            List<TabSectionRequest> tabsReq = foodBeverageRequest.getTabsSection();
            tabsImpl.addTabs(tabsReq, lastServiceAdded);

            // Set Policies
            List<PolicySectionRequest> policyReq = foodBeverageRequest.getPolicySection();
            lastServiceAdded.setPolicies(policyImpl.addPolicies(policyReq, category, lastServiceAdded));

            // Upload and associate images
            imageService.uploadImagesForService(images, lastServiceAdded);

            // Set the availability slots
            List<AvailableTimeDTO> availabilitySlots = foodBeverageRequest.getAvailableTimeDTOS();
            if (availabilitySlots == null) {
                throw new BadRequestException("Availability Slots cannot be empty");
            }
            servicesForAll.setAvailableTime(availabilitySlots, lastServiceAdded);
            foodBeverageRepository.save(lastServiceAdded);

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
    public APIResponse<FoodBeverageResponse> getAll(Integer pageNumber, Integer pageSize) {
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize);

        List<FoodAndBeverage> foodBeveragePage = foodBeverageRepository.findByProvider_UserId(authUtils.loggedInUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Food and Beverage", authUtils.loggedInUserId()));

//        List<FoodAndBeverage> foodBeverageServices=foodBeveragePage.getContent();

//        if (foodBeverageServices.isEmpty())
//            throw new APIException("No Food and Beverage service created till now");

        List<FoodBeverageRequest> foodBeverage_DTOs = new ArrayList<>();

        for (FoodAndBeverage foodAndBeverage : foodBeveragePage) {
            FoodBeverageRequest foodBeverageServiceRequest = new FoodBeverageRequest();
            if (foodAndBeverage.getStatus() == ServiceStatus.ACTIVE) {
                //set the images
                List<Image> images = imageRepository.findByService_ServiceId(foodAndBeverage.getServiceId());
                //map images to imageDTO
                List<ImageRequestDTO> imgDTOs = new ArrayList<>();
                for (Image img : images) {
                    ImageRequestDTO imgDTO = new ImageRequestDTO();
                    imgDTO.setId(img.getImageId());
                    imgDTO.setImageUrl(img.getImageUrl());
                    imgDTOs.add(imgDTO);

                }
                foodBeverageServiceRequest.setServiceId(foodAndBeverage.getServiceId());
                foodBeverageServiceRequest.setServiceName(foodAndBeverage.getServiceName());
                foodBeverageServiceRequest.setStatus(foodAndBeverage.getStatus());
                foodBeverageServiceRequest.setImages(imgDTOs);
                // Safely get average rating with null check
                APIResponse<RateAndReviewResponse> ratingResponse = reviewService.getAverageRatingByServiceId(foodAndBeverage.getServiceId());
                Double averageRating = (ratingResponse != null && ratingResponse.getData() != null)
                        ? ratingResponse.getData().getAverageRating()
                        : 0.0;
                foodBeverageServiceRequest.setAverageRating(averageRating);

                foodBeverageServiceRequest.setTotalBookingsForPastMonth(bookingService.countBookingsForServiceInPeriod(foodAndBeverage.getServiceId(), LocalDateTime.now().minusMonths(1), LocalDateTime.now()));
                foodBeverage_DTOs.add(foodBeverageServiceRequest);
            }

        }

        FoodBeverageResponse foodBeverageResponse = new FoodBeverageResponse();

        foodBeverageResponse.setContent(foodBeverage_DTOs);
//        foodBeverageResponse.setLastPage(foodBeveragePage.isLast());
//        foodBeverageResponse.setPageNumber(foodBeveragePage.getNumber());
//        foodBeverageResponse.setPageSize(foodBeveragePage.getSize());
//        foodBeverageResponse.setTotalElements(foodBeveragePage.getTotalElements());
//        foodBeverageResponse.setTotalPages(foodBeveragePage.getTotalPages());
        return APIResponse.<FoodBeverageResponse>builder()
                .success(true)
                .message("Food and Beverages services Fetched")
                .data(foodBeverageResponse)
                .build();

    }

    @Override
    @Transactional
    public APIResponse<FoodBeverageRequest> searchWithId(Long Id) {
        FoodAndBeverage foodAndBeverage = foodBeverageRepository.findById(Id)
                .orElseThrow(() -> new ResourceNotFoundException("Food and Beverage", Id));

        List<TabsSection> tabsSection = tabsSectionRepository.findByService_ServiceId(Id);
        List<TabSectionRequest> tabs = new ArrayList<>();

        for (TabsSection tab : tabsSection) {
            TabSectionRequest tabReq = new TabSectionRequest();
            tabReq.setId(tab.getId());
            tabReq.setHeading(tab.getHeading());
            tabReq.setContent(tab.getContent());
            tabs.add(tabReq);
        }

//        List<PolicySection> policySection = policySectionRepository.findByProviderIdAndCategoryIdOrNull(authUtils.loggedInUserId(),3L);
        List<PolicySection> policySection = foodAndBeverage.getPolicies().stream().toList();
        List<PolicySectionRequest> policies = new ArrayList<>();
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

        FoodBeverageRequest prepareResponse = new FoodBeverageRequest();

        prepareResponse.setServiceName(foodAndBeverage.getServiceName());
//        prepareResponse.setOpenHours(foodAndBeverage.getOpenHours());
        prepareResponse.setFoodAndBeverageType(foodAndBeverage.getFoodAndBeverageCategory().getCategoryName());
        prepareResponse.setVegetarianOptions(foodAndBeverage.getVegetarianOptions());
        prepareResponse.setHalalCertified(foodAndBeverage.getHalalCertified());
        prepareResponse.setAlcoholServed(foodAndBeverage.getAlcoholServed());
        prepareResponse.setOutdoorSeating(foodAndBeverage.getOutdoorSeating());
        prepareResponse.setLiveMusic(foodAndBeverage.getLiveMusic());
        prepareResponse.setCuisineType(foodAndBeverage.getCuisineType());
        prepareResponse.setLocations(foodAndBeverage.getLocations().stream()
                .map(location -> modelMapper.map(location, LocationDTO.class))
                .collect(Collectors.toSet()));
        prepareResponse.setContactNo(foodAndBeverage.getContactNo());
        prepareResponse.setTabsSection(tabs);
        prepareResponse.setPolicySection(policies);
        prepareResponse.setImages(imgDTOs);
        prepareResponse.setPriceConfig(modelMapper.map(foodAndBeverage.getPriceConfiguration(), PriceConfigDTO.class));
        prepareResponse.setBookingConfig(modelMapper.map(foodAndBeverage.getBookingConfiguration(), BookingConfigDTO.class));
        prepareResponse.setServiceId(Id);
        prepareResponse.setStatus(foodAndBeverage.getStatus());
        prepareResponse.setAvailableTimeDTOS(foodAndBeverage.getAvailableTimes().stream()
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

        return APIResponse.<FoodBeverageRequest>builder()
                .success(true)
                .message("Fetched Food and Beverage Service ")
                .data(prepareResponse)
                .build();

    }

    @Override
    @Transactional
    //Adding new policy for the entire activity category
    public APIResponse<String> addNewPolicy(PolicySection policies) {
        Category category = categoryRepository.findByCategoryName(ServiceCategory.FOOD_BEVERAGE)
                .orElseThrow(() -> new ResourceNotFoundException("Category", 3L));

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
    public APIResponse<String> updateService(Long id, FoodBeverageRequest foodBeverageRequest, List<MultipartFile> images) {
        FoodAndBeverage foodAndBeverageService = foodBeverageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Food and Beverage", foodBeverageRequest.getServiceId()));

        FoodAndBeverageCategory foodAndBeverageCategory = foodAndBeverageCategoryRepository
                .findByCategoryName(foodBeverageRequest.getFoodAndBeverageType())
                .orElseThrow(() -> new ResourceNotFoundException("Food and Beverage Category", foodBeverageRequest.getFoodAndBeverageType().getDisplayName()));

        // Update the fields
        foodAndBeverageService.setServiceName(foodBeverageRequest.getServiceName());
//        foodAndBeverageService.setStatus(foodBeverageRequest.getStatus());
        foodAndBeverageService.setContactNo(foodBeverageRequest.getContactNo());
//        foodAndBeverageService.setOpenHours(foodBeverageRequest.getOpenHours());
        foodAndBeverageService.setVegetarianOptions(foodBeverageRequest.getVegetarianOptions());
        foodAndBeverageService.setHalalCertified(foodBeverageRequest.getHalalCertified());
        foodAndBeverageService.setAlcoholServed(foodBeverageRequest.getAlcoholServed());
        foodAndBeverageService.setOutdoorSeating(foodBeverageRequest.getOutdoorSeating());
        foodAndBeverageService.setLiveMusic(foodBeverageRequest.getLiveMusic());
        foodAndBeverageService.setCuisineType(foodBeverageRequest.getCuisineType());
        foodAndBeverageService.setContactNo(foodBeverageRequest.getContactNo());
        foodAndBeverageService.setFoodAndBeverageCategory(foodAndBeverageCategory);

        // Update the locations
        foodAndBeverageService.setLocations(servicesForAll.setServiceLocation(foodBeverageRequest));
        // Update configurations
        foodAndBeverageService.setBookingConfiguration(servicesForAll.setBookingConfig(foodBeverageRequest.getBookingConfig()));
        foodAndBeverageService.setPriceConfiguration(servicesForAll.setPriceConfig(foodBeverageRequest.getPriceConfig()));

        // Save the updated service
        foodAndBeverageService = foodBeverageRepository.save(foodAndBeverageService);

        // Set the availability slots
        List<AvailableTimeDTO> availabilitySlots = foodBeverageRequest.getAvailableTimeDTOS();
        if (availabilitySlots == null) {
            throw new BadRequestException("Availability Slots cannot be empty");
        }
        servicesForAll.setAvailableTime(availabilitySlots, foodAndBeverageService);

        // Update the tabs
        tabsImpl.updateTabs(foodBeverageRequest.getTabsSection(), foodAndBeverageService);
        tabsImpl.deleteTabs(foodBeverageRequest.getDeletedTabs());

        // Update the policies
        policyImpl.updatePolicies(foodBeverageRequest.getPolicySection(), foodAndBeverageService);
        policyImpl.deletePolicies(foodBeverageRequest.getDeletedPolicies(), foodAndBeverageService);

        // Upload and associate images
        if (images != null && !images.isEmpty()) {
            imageService.uploadImagesForService(images, foodAndBeverageService);
        }

        //Delete images if specified
        if (foodBeverageRequest.getDeletedImages() != null && !foodBeverageRequest.getDeletedImages().isEmpty()) {
            for (ImageRequestDTO imageReq : foodBeverageRequest.getDeletedImages()) {
                imageService.deleteImages(foodBeverageRequest.getDeletedImages());
            }
        }

        return APIResponse.<String>builder()
                .success(true)
                .message("Food and Beverage Service Updated Successfully")
                .data("")
                .build();
    }

    @Override
    public APIResponse<String> deleteService(Long Id) {
        FoodAndBeverage foodAndBeverage = foodBeverageRepository.findById(Id)
                .orElseThrow(() -> new ResourceNotFoundException("Food and Beverage", Id));

        foodAndBeverage.setStatus(ServiceStatus.INACTIVE);
        foodBeverageRepository.save(foodAndBeverage);

        return APIResponse.<String>builder()
                .success(true)
                .message("Food and Beverage Service Deleted Successfully")
                .data("")
                .build();
    }

}
