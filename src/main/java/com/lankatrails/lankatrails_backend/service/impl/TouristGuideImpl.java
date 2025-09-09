package com.lankatrails.lankatrails_backend.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.lankatrails.lankatrails_backend.dtos.request.AvailableTimeDTO;
import com.lankatrails.lankatrails_backend.dtos.request.BookingConfigDTO;
import com.lankatrails.lankatrails_backend.dtos.request.BreakTimeDTO;
import com.lankatrails.lankatrails_backend.dtos.request.ImageRequestDTO;
import com.lankatrails.lankatrails_backend.dtos.request.LocationDTO;
import com.lankatrails.lankatrails_backend.dtos.request.PolicySectionRequest;
import com.lankatrails.lankatrails_backend.dtos.request.PriceConfigDTO;
import com.lankatrails.lankatrails_backend.dtos.request.TabSectionRequest;
import com.lankatrails.lankatrails_backend.dtos.request.TouristGuideRequestDTO;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.TouristGuideResponseDTO;
import com.lankatrails.lankatrails_backend.exception.APIException;
import com.lankatrails.lankatrails_backend.exception.BadRequestException;
import com.lankatrails.lankatrails_backend.exception.ResourceNotFoundException;
import com.lankatrails.lankatrails_backend.exception.ServiceAlreadyExistsException;
import com.lankatrails.lankatrails_backend.model.BookingConfiguration;
import com.lankatrails.lankatrails_backend.model.Category;
import com.lankatrails.lankatrails_backend.model.Image;
import com.lankatrails.lankatrails_backend.model.Language;
import com.lankatrails.lankatrails_backend.model.PolicySection;
import com.lankatrails.lankatrails_backend.model.PriceConfiguration;
import com.lankatrails.lankatrails_backend.model.Provider;
import com.lankatrails.lankatrails_backend.model.TabsSection;
import com.lankatrails.lankatrails_backend.model.TourGuideCategory;
import com.lankatrails.lankatrails_backend.model.TouristGuide;
import com.lankatrails.lankatrails_backend.model.enums.ServiceCategory;
import com.lankatrails.lankatrails_backend.model.enums.ServiceStatus;
import com.lankatrails.lankatrails_backend.repositories.CategoryRepository;
import com.lankatrails.lankatrails_backend.repositories.ImageRepository;
import com.lankatrails.lankatrails_backend.repositories.LanguageRepository;
import com.lankatrails.lankatrails_backend.repositories.PolicySectionRepository;
import com.lankatrails.lankatrails_backend.repositories.ProviderRepository;
import com.lankatrails.lankatrails_backend.repositories.TabsSectionRepository;
import com.lankatrails.lankatrails_backend.repositories.TourGuideCategoryRepository;
import com.lankatrails.lankatrails_backend.repositories.TouristGuideRepository;
import com.lankatrails.lankatrails_backend.security.utils.AuthUtils;
import com.lankatrails.lankatrails_backend.service.ServicesForAll;
import com.lankatrails.lankatrails_backend.service.TouristGuideService;
import com.lankatrails.lankatrails_backend.service.utils.FileUploadService;

@Service
public class TouristGuideImpl implements TouristGuideService {
    @Autowired
    private TouristGuideRepository touristGuideRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PolicySectionRepository policySectionRepository;

    @Autowired
    private TabsSectionRepository tabsSectionRepository;

    @Autowired
    private LanguageRepository languageRepository;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private TourGuideCategoryRepository tourGuideCategoryRepository;

    @Autowired
    private ProviderRepository providerRepository;

    @Autowired
    ServicesForAll servicesForAll;

    @Autowired
    TabsImpl tabsImpl;

    @Autowired
    PolicyImpl policyImpl;

    @Autowired
    private ImageServiceImpl imageService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private AuthUtils authUtils;

    @Autowired
    private FileUploadService fileUploadService;

    @Override
    public APIResponse<TouristGuideResponseDTO> getAllTourGuides(Integer pageNumber, Integer pageSize) {
        Pageable pageDetails= PageRequest.of(pageNumber,pageSize);

        Page<TouristGuide> touristGuides=touristGuideRepository.findAll(pageDetails);

        List<TouristGuide> guidesContent=touristGuides.getContent();

        if (guidesContent.isEmpty())
            throw new APIException("No tourist guide created till now");

        List<TouristGuideRequestDTO> tourGuideReq_DTOs= new ArrayList<>();

        for (TouristGuide guide : guidesContent){
            TouristGuideRequestDTO tourGuideRequest = new TouristGuideRequestDTO();
            if (guide.getStatus() == ServiceStatus.ACTIVE){
                tourGuideRequest.setServiceId(guide.getServiceId());
                tourGuideRequest.setServiceName(guide.getServiceName());
                tourGuideRequest.setStatus(guide.getStatus());
                tourGuideReq_DTOs.add(tourGuideRequest);
            }

        }

        TouristGuideResponseDTO tourGuideResponse=new TouristGuideResponseDTO();

        tourGuideResponse.setContent(tourGuideReq_DTOs);
        tourGuideResponse.setLastPage(touristGuides.isLast());
        tourGuideResponse.setPageNumber(touristGuides.getNumber());
        tourGuideResponse.setPageSize(touristGuides.getSize());
        tourGuideResponse.setTotalElements(touristGuides.getTotalElements());
        tourGuideResponse.setTotalPages(touristGuides.getTotalPages());
        return  APIResponse.<TouristGuideResponseDTO>builder()
                .success(true)
                .message("Tourist Guides Fetched")
                .data(tourGuideResponse)
                .build();

    }

    @Override
    @Transactional
    public TouristGuideResponseDTO addNewTouristGuide(TouristGuideRequestDTO requestDTO, List<MultipartFile> images) {
        TouristGuide mappedObj = modelMapper.map(requestDTO, TouristGuide.class);

        Category category = categoryRepository.findByCategoryName(ServiceCategory.TOUR_GUIDE)
                .orElseThrow(() -> new ResourceNotFoundException("Category", 4L));

        Provider provider = providerRepository.findById(authUtils.loggedInUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Provider", authUtils.loggedInUserId()));
        mappedObj.setCategory(category);
        mappedObj.setProvider(provider);

        mappedObj.setLocations(servicesForAll.setServiceLocation(requestDTO));
        mappedObj.setBookingConfiguration(servicesForAll.setBookingConfig(requestDTO.getBookingConfig()));
        mappedObj.setPriceConfiguration(servicesForAll.setPriceConfig(requestDTO.getPriceConfig()));
        mappedObj.setStatus(ServiceStatus.ACTIVE);

        Optional<TouristGuide> checkDb = touristGuideRepository.findByServiceName(mappedObj.getServiceName());

        if (checkDb.isEmpty()) {
            TourGuideCategory tourGuideCategory = tourGuideCategoryRepository
                    .findByCategoryName(requestDTO.getTourGuideType())
                    .orElseThrow(() -> new ResourceNotFoundException("Tour Guide Category", requestDTO.getTourGuideType().name()));
            mappedObj.setTourGuideCategory(tourGuideCategory);

            // Save Tourist Guide first
            TouristGuide lastGuideAdded = touristGuideRepository.save(mappedObj);

            // Set Tabs
            List<TabSectionRequest> tabsReq = requestDTO.getTabsSection();
            tabsImpl.addTabs(tabsReq, lastGuideAdded);

            // Set Policies
            List<PolicySectionRequest> policyReq = requestDTO.getPolicySection();
            lastGuideAdded.setPolicies(policyImpl.addPolicies(policyReq, category, lastGuideAdded));

            // Upload and associate images
            imageService.uploadImagesForService(images, lastGuideAdded);
            // Set the availability slots
            List<AvailableTimeDTO> availabilitySlots = requestDTO.getAvailableTimeDTOS();
            if (availabilitySlots == null ){
                throw new BadRequestException("Availability Slots cannot be empty");
            }
            servicesForAll.setAvailableTime(availabilitySlots, lastGuideAdded);

            //save the languages served
            List<String> languageList = requestDTO.getLanguages();
            Set<Language> languages = new HashSet<>();
            languageList.forEach(lng ->{
                Language language = new Language();
                language.setLanguage(lng);
                language.setTouristGuide(lastGuideAdded);
                languages.add(language);
            });
            languageRepository.saveAll(languages);
            touristGuideRepository.save(lastGuideAdded);
            
            // Prepare response
            TouristGuideRequestDTO responseDTO = new TouristGuideRequestDTO();
            responseDTO.setServiceName(requestDTO.getServiceName());
            responseDTO.setLocations(requestDTO.getLocations());
            responseDTO.setContactNo(requestDTO.getContactNo());
            responseDTO.setLanguages(requestDTO.getLanguages());
            responseDTO.setTourGuideType(requestDTO.getTourGuideType());
            responseDTO.setTabsSection(tabsReq);
            responseDTO.setPolicySection(policyReq);

            TouristGuideResponseDTO response = new TouristGuideResponseDTO();
            response.setContent(List.of(responseDTO));
            return response;

        } else {
            throw new ServiceAlreadyExistsException(checkDb.get().getServiceId());
        }
    }

    @Override
    @Transactional
    public APIResponse<TouristGuideRequestDTO> searchWithId(Long id) {
        TouristGuide touristGuide=touristGuideRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Tour Guide",id));

        List<TabsSection> tabSection=tabsSectionRepository.findByService_ServiceId(id);
        List<TabSectionRequest> tabs=new ArrayList<>();

        //get the tabs
        for(TabsSection tab: tabSection){
            TabSectionRequest tabReq=new TabSectionRequest();
            tabReq.setId(tab.getId());
            tabReq.setHeading(tab.getHeading());
            tabReq.setContent(tab.getContent());
            tabs.add(tabReq);
        }

//        List<PolicySection> policySection = policySectionRepository.findByProviderIdAndCategoryIdOrNull(authUtils.loggedInUserId(),4L);

        List<PolicySection> policySection = touristGuide.getPolicies().stream().toList();
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
        List<Image> images = imageRepository.findByService_ServiceId(id);
        //map images to imageDTO
        List<ImageRequestDTO> imgDTOs = new ArrayList<>();
        for (Image img : images){
            ImageRequestDTO imgDTO = new ImageRequestDTO();
            imgDTO.setId(img.getImageId());
            imgDTO.setImageUrl(img.getImageUrl());
            imgDTOs.add(imgDTO);

        }

        //prepare the response
        TouristGuideRequestDTO prepareResponse=new TouristGuideRequestDTO();
        prepareResponse.setServiceId(id);
        prepareResponse.setPriceConfig(modelMapper.map(touristGuide.getPriceConfiguration(),PriceConfigDTO.class));
        prepareResponse.setBookingConfig(modelMapper.map(touristGuide.getBookingConfiguration(),BookingConfigDTO.class));
        prepareResponse.setImages(imgDTOs);
        prepareResponse.setTourGuideType(touristGuide.getTourGuideCategory().getCategoryName());
        prepareResponse.setServiceName(touristGuide.getServiceName());
        prepareResponse.setContactNo(touristGuide.getContactNo());
        prepareResponse.setLocations(touristGuide.getLocations().stream()
                .map(location -> modelMapper.map(location, LocationDTO.class))
                .collect(Collectors.toSet()));
        prepareResponse.setPolicySection(policies);
        prepareResponse.setTabsSection(tabs);
        prepareResponse.setLanguages(touristGuide.getLanguages()
                        .stream()
                        .map(Language::getLanguage)
                        .collect(Collectors.toList())
        );
        prepareResponse.setStatus(touristGuide.getStatus());
        prepareResponse.setAvailableTimeDTOS(touristGuide.getAvailableTimes().stream()
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


        return APIResponse.<TouristGuideRequestDTO>builder()
                .success(true)
                .message("Found Tourist Guide")
                .data(prepareResponse)
                .build();

    }

    @Override
    @Transactional
    public TouristGuideResponseDTO updateTourGuide(Long id, TouristGuideRequestDTO requestDTO) {

        TouristGuide touristGuide=touristGuideRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Tour Guide",id));

        //update the tourist guide
        touristGuide.setServiceName(requestDTO.getServiceName());
        touristGuide.setContactNo(requestDTO.getContactNo());
        touristGuide.setStatus(requestDTO.getStatus());
        
        // Update locations
        if (requestDTO.getLocations() != null && !requestDTO.getLocations().isEmpty()) {
            touristGuide.setLocations(servicesForAll.setServiceLocation(requestDTO));
        }

        // Update configurations
        touristGuide.setBookingConfiguration(servicesForAll.setBookingConfig(requestDTO.getBookingConfig()));
        touristGuide.setPriceConfiguration(servicesForAll.setPriceConfig(requestDTO.getPriceConfig()));

        //save the updated tour guide
        TouristGuide updatedTourGuide = touristGuideRepository.save(touristGuide);

        // Set the availability slots
        List<AvailableTimeDTO> availabilitySlots = requestDTO.getAvailableTimeDTOS();
        if (availabilitySlots == null ){
            throw new BadRequestException("Availability Slots cannot be empty");
        }
        servicesForAll.setAvailableTime(availabilitySlots, updatedTourGuide);

        // Update tabs
        tabsImpl.updateTabs(requestDTO.getTabsSection(), updatedTourGuide);
        tabsImpl.deleteTabs(requestDTO.getDeletedTabs());

        // Update policies
        policyImpl.updatePolicies(requestDTO.getPolicySection(), updatedTourGuide);
        policyImpl.deletePolicies(requestDTO.getDeletedPolicies(), updatedTourGuide);

        TouristGuideRequestDTO responseDTO=modelMapper.map(touristGuideRepository.findById(id),TouristGuideRequestDTO.class);

        List<TouristGuideRequestDTO> responseList=new ArrayList<>();
        responseList.add(responseDTO);

        TouristGuideResponseDTO sendResponse=new TouristGuideResponseDTO();
        sendResponse.setContent(responseList);

        return sendResponse;
    }

    @Override
    @Transactional
    //Adding new policy for the entire tour-guide category
    public APIResponse<String> addNewPolicy(PolicySection policies) {
        Category category = categoryRepository.findByCategoryName(ServiceCategory.TOUR_GUIDE)
                .orElseThrow(() -> new ResourceNotFoundException("Category", 5L));

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
    public APIResponse<String> updateService(Long id, TouristGuideRequestDTO requestDTO, List<MultipartFile> images) {
        TouristGuide touristGuide = touristGuideRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tourist Guide", id));

        TourGuideCategory tourGuideCategory = tourGuideCategoryRepository
                .findByCategoryName(requestDTO.getTourGuideType())
                .orElseThrow(() -> new ResourceNotFoundException("Tour Guide Category", requestDTO.getTourGuideType().name()));

        // Update the tourist guide details
        touristGuide.setServiceName(requestDTO.getServiceName());
        touristGuide.setContactNo(requestDTO.getContactNo());
        touristGuide.setPriceConfiguration(modelMapper.map(requestDTO.getPriceConfig(), PriceConfiguration.class));
        touristGuide.setBookingConfiguration(modelMapper.map(requestDTO.getBookingConfig(), BookingConfiguration.class));
        touristGuide.setTourGuideCategory(tourGuideCategory);

        // handle languages
        if (requestDTO.getLanguages() != null && !requestDTO.getLanguages().isEmpty()) {
            // Clear existing languages and set new ones
            languageRepository.deleteAll(touristGuide.getLanguages());
            touristGuide.setLanguages(requestDTO.getLanguages()
                    .stream()
                    .map(language -> {
                        Language lang = new Language();
                        lang.setLanguage(language);
                        lang.setTouristGuide(touristGuide);
                        return languageRepository.save(lang);
                    })
                    .collect(Collectors.toSet()));

        }

        // Update locations
        touristGuide.setLocations(servicesForAll.setServiceLocation(requestDTO));

        // Save the updated tourist guide
        TouristGuide updatedTourGuide = touristGuideRepository.save(touristGuide);

        // update tabs
        tabsImpl.updateTabs(requestDTO.getTabsSection(), updatedTourGuide);
        tabsImpl.deleteTabs(requestDTO.getDeletedTabs());

        // update policies
        policyImpl.updatePolicies(requestDTO.getPolicySection(), updatedTourGuide);
        policyImpl.deletePolicies(requestDTO.getDeletedPolicies(), updatedTourGuide);

        // Handle image uploads
        if (images != null && !images.isEmpty()) {
            imageService.uploadImagesForService(images, updatedTourGuide);
        }

        // Delete images if specified
        if (requestDTO.getDeletedImages() != null && !requestDTO.getDeletedImages().isEmpty()) {
            imageService.deleteImages(requestDTO.getDeletedImages());
        }

        return APIResponse.<String>builder()
                .success(true)
                .message("Tourist Guide Updated Successfully")
                .data("Tourist Guide with ID " + id + " has been updated.")
                .build();
    }

    @Override
    public APIResponse<String> deleteService(Long Id) {
        TouristGuide touristGuide = touristGuideRepository.findById(Id)
                .orElseThrow(() -> new ResourceNotFoundException("Tourist Guide", Id));

        touristGuide.setStatus(ServiceStatus.INACTIVE);
        touristGuideRepository.save(touristGuide);
        return APIResponse.<String>builder()
                .success(true)
                .message("Tourist Guide Deleted Successfully")
                .data("")
                .build();
    }

}
