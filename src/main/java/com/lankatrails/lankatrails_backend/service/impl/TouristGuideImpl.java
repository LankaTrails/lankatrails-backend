package com.lankatrails.lankatrails_backend.service.impl;

import com.lankatrails.lankatrails_backend.dtos.request.*;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.ActivityServiceResponse;
import com.lankatrails.lankatrails_backend.dtos.response.TouristGuideResponseDTO;
import com.lankatrails.lankatrails_backend.exception.APIException;
import com.lankatrails.lankatrails_backend.exception.ResourceNotFoundException;
import com.lankatrails.lankatrails_backend.exception.ServiceAlreadyExistsException;
import com.lankatrails.lankatrails_backend.model.*;
import com.lankatrails.lankatrails_backend.model.enums.ServiceCategory;
import com.lankatrails.lankatrails_backend.model.enums.UploadCategory;
import com.lankatrails.lankatrails_backend.repositories.*;
import com.lankatrails.lankatrails_backend.security.utils.AuthUtils;
import com.lankatrails.lankatrails_backend.service.TouristGuideService;
import com.lankatrails.lankatrails_backend.service.utils.FileUploadService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    private AreaRepository areaRepository;

    @Autowired
    private TourGuideCategoryRepository tourGuideCategoryRepository;

    @Autowired
    private ServiceAreaRepository serviceAreaRepository;

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
            if (guide.getStatus()){
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

        Provider provider = (Provider) authUtils.loggedInUser();
        mappedObj.setCategory(category);
        mappedObj.setProvider(provider);

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
            if (tabsReq != null) {
                for (TabSectionRequest tab : tabsReq) {
                    TabsSection tabsSection = new TabsSection();
                    tabsSection.setHeading(tab.getHeading());
                    tabsSection.setContent(tab.getContent());
                    tabsSection.setService(lastGuideAdded);
                    tabsSectionRepository.save(tabsSection);
                }
            }

            // Set Policies
            List<PolicySectionRequest> policyReq = requestDTO.getPolicySection();
            if (policyReq != null) {
                for (PolicySectionRequest policy : policyReq) {
                    PolicySection policySection = new PolicySection();
                    policySection.setHeading(policy.getHeading());
                    policySection.setPolicy(policy.getPolicy());
                    policySection.setProvider(lastGuideAdded.getProvider());
                    policySectionRepository.save(policySection);
                }
            }

            // Upload and associate images
            Set<Image> savedImages = new HashSet<>();
            for (MultipartFile file : images) {
                String imageUrl = fileUploadService.storeFile(file, UploadCategory.SERVICE_PICTURE, "service");

                Image image = new Image();
                image.setImageUrl(imageUrl);
                image.setService(lastGuideAdded);

                savedImages.add(image);
            }

            imageRepository.saveAll(savedImages); // Persist images

            //save the areas served
            List<String> areaList = requestDTO.getServiceAreas();
            Set<GuidingArea> guidingAreas = new HashSet<>();
            areaList.forEach(area ->{
                GuidingArea setAreas =new GuidingArea();
                setAreas.setServiceArea(area);
                setAreas.setTouristGuide(lastGuideAdded);
                guidingAreas.add(setAreas);

            });
            areaRepository.saveAll(guidingAreas);

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
            // Prepare response
            TouristGuideRequestDTO responseDTO = new TouristGuideRequestDTO();
            responseDTO.setServiceName(requestDTO.getServiceName());
            responseDTO.setLocationBased(requestDTO.getLocationBased());
            responseDTO.setContactNo(requestDTO.getContactNo());
//            responseDTO.setServiceAreas(requestDTO.getServiceAreas());
//            responseDTO.setLanguages(requestDTO.getLanguages());
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
        List<Image> images = imageRepository.findByService_ServiceId(id);
        //map images to imageDTO
        List<ImageRequestDTO> imgDTOs = new ArrayList<>();
        for (Image img : images){
            ImageRequestDTO imgDTO = new ImageRequestDTO();
            imgDTO.setImageUrl(img.getImageUrl());
            imgDTOs.add(imgDTO);

        }

        Optional<GuidingArea> guidingAreas = serviceAreaRepository.findByTouristGuide_ServiceId(id);

        //prepare the response
        TouristGuideRequestDTO prepareResponse=new TouristGuideRequestDTO();
        prepareResponse.setServiceId(id);
        prepareResponse.setPrice(touristGuide.getPrice());
        prepareResponse.setImages(imgDTOs);
        prepareResponse.setPriceType(touristGuide.getPriceType());
        prepareResponse.setTourGuideType(touristGuide.getTourGuideCategory().getCategoryName());
        prepareResponse.setServiceName(touristGuide.getServiceName());
        prepareResponse.setContactNo(touristGuide.getContactNo());
        prepareResponse.setLocationBased(modelMapper.map(touristGuide.getLocationBased(), LocationDTO.class));
        prepareResponse.setPolicySection(policies);
        prepareResponse.setTabsSection(tabs);
        prepareResponse.setLanguages(touristGuide.getLanguages()
                        .stream()
                        .map(Language::getLanguage)
                        .collect(Collectors.toList())
        );
        prepareResponse.setServiceAreas(guidingAreas.stream()
                .map(GuidingArea ::getServiceArea)
                .collect(Collectors.toList()));



        
//        prepareResponse.setServiceAreas(touristGuide.getServiceAreas());


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
//        touristGuide.setLocationBased(requestDTO.getLocationBased());
        touristGuide.setContactNo(requestDTO.getContactNo());
        touristGuide.setStatus(requestDTO.getStatus());
//        touristGuide.setLanguages(requestDTO.getLanguages());
//        touristGuide.setServiceAreas(requestDTO.getServiceAreas());

        //save the updated tour guide
        touristGuideRepository.save(touristGuide);

        //update or add tabs
        //get the tabs from the database
        Set<TabsSection> tabs=touristGuide.getTabs();

        //get the tabs from the request
        List<TabSectionRequest> reqTabs=requestDTO.getTabsSection();

        //create a map of existing tabs by ID for quick lookup
        Map<Long,TabsSection> savedTabMap=tabs.stream()
                .collect(Collectors.toMap(TabsSection::getId, Function.identity()));

        //create a set to track updated or newly added tabs
        Set<TabsSection> updatedTabs=new HashSet<>();

        for(TabSectionRequest req:reqTabs){
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
                tab.setService(touristGuide);
            }
            updatedTabs.add(tab);
        }
        tabsSectionRepository.saveAll(updatedTabs);

        //update or add policies
        Set<PolicySection> policies=touristGuide.getPolicies();

        //get the policySection from the request
        List<PolicySectionRequest> reqPolicies=requestDTO.getPolicySection();
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
                policySection.setProvider(touristGuide.getProvider());
            }
            updatedPolicies.add(policySection);

        }

        TouristGuideRequestDTO responseDTO=modelMapper.map(touristGuideRepository.findById(id),TouristGuideRequestDTO.class);
        responseDTO.setTabsSection(reqTabs);
        responseDTO.setPolicySection(reqPolicies);

        List<TouristGuideRequestDTO> responseList=new ArrayList<>();
        responseList.add(responseDTO);

        TouristGuideResponseDTO sendResponse=new TouristGuideResponseDTO();
        sendResponse.setContent(responseList);

        return sendResponse;
    }

}
