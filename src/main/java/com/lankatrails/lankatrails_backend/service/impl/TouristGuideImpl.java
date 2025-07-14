package com.lankatrails.lankatrails_backend.service.impl;

import com.lankatrails.lankatrails_backend.dtos.request.PolicySectionRequest;
import com.lankatrails.lankatrails_backend.dtos.request.TabSectionRequest;
import com.lankatrails.lankatrails_backend.dtos.request.TouristGuideRequestDTO;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.TouristGuideResponseDTO;
import com.lankatrails.lankatrails_backend.exception.ResourceNotFoundException;
import com.lankatrails.lankatrails_backend.exception.ServiceAlreadyExistsException;
import com.lankatrails.lankatrails_backend.model.*;
import com.lankatrails.lankatrails_backend.model.enums.ServiceCategory;
import com.lankatrails.lankatrails_backend.repositories.*;
import com.lankatrails.lankatrails_backend.security.utils.AuthUtils;
import com.lankatrails.lankatrails_backend.service.TouristGuideService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private ModelMapper modelMapper;

    @Autowired
    private AuthUtils authUtils;

    @Override
    public TouristGuideResponseDTO getAllTourGuides() {
        List<TouristGuide> allGuides=touristGuideRepository.findAll();
        List<TouristGuideRequestDTO> responseList=new ArrayList<>();
        for (TouristGuide guide : allGuides){
            TouristGuideRequestDTO response=new TouristGuideRequestDTO();
            response.setServiceAreas(guide.getServiceAreas());
            response.setServiceName(guide.getServiceName());
            response.setContactNo(guide.getContactNo());

            //find the languages provided by a respective tour guide
            List<Language> languages=languageRepository.findByTouristGuide_ServiceId(guide.getServiceId());
//            if (!languages.isEmpty()){
                response.setLanguages(languages);
//            }
            responseList.add(response);

        }
        TouristGuideResponseDTO touristGuideResponseDTO=new TouristGuideResponseDTO() ;
        touristGuideResponseDTO.setContent(responseList);

        return touristGuideResponseDTO;

    }

    @Override
    public TouristGuideResponseDTO addNewTouristGuide(TouristGuideRequestDTO requestDTO) {
        TouristGuide mappedObj=modelMapper.map(requestDTO,TouristGuide.class);

        Category category=categoryRepository.findByCategoryName(ServiceCategory.TOUR_GUIDE)
                .orElseThrow(()->new ResourceNotFoundException("Category",4L));

        Provider provider=(Provider) authUtils.loggedInUser();
        mappedObj.setCategory(category);
        mappedObj.setProvider(provider);

        Optional<TouristGuide> checkDb=touristGuideRepository.findByServiceName(mappedObj.getServiceName());

        if (checkDb.isEmpty()){
            TouristGuide lastGuideAdded=touristGuideRepository.save(mappedObj);

            //set the tabs
            List<TabSectionRequest> tabsReq=requestDTO.getTabsSection();
            if (tabsReq!=null){
                for (TabSectionRequest tab : tabsReq){
                    TabsSection tabsSection=new TabsSection();
                    tabsSection.setHeading(tab.getHeading());
                    tabsSection.setContent(tab.getContent());
                    tabsSection.setService(lastGuideAdded);
                    tabsSectionRepository.save(tabsSection);
                }
            }


            //set the policies
            List<PolicySectionRequest> policyReq=requestDTO.getPolicySection();

            if (policyReq!=null){
                for (PolicySectionRequest policy:policyReq){
                    PolicySection policySection=new PolicySection();
                    policySection.setHeading(policy.getHeading());
                    policySection.setPolicy(policy.getPolicy());
                    policySection.setProvider(lastGuideAdded.getProvider());
                    policySectionRepository.save(policySection);
                }
            }

            //set the response
            TouristGuideRequestDTO responseDTO=new TouristGuideRequestDTO();
            responseDTO.setServiceName(requestDTO.getServiceName());
            responseDTO.setLocationBased(requestDTO.getLocationBased());
            responseDTO.setContactNo(requestDTO.getContactNo());
            responseDTO.setServiceAreas(requestDTO.getServiceAreas());
            responseDTO.setLanguages(requestDTO.getLanguages());
            responseDTO.setTabsSection(tabsReq);
            responseDTO.setPolicySection(policyReq);

            TouristGuideResponseDTO response=new TouristGuideResponseDTO();
            List<TouristGuideRequestDTO> responseList=new ArrayList<>();
            responseList.add(responseDTO);
            response.setContent(responseList);
            return response;

        }else{
            throw new ServiceAlreadyExistsException(checkDb.get().getServiceId());
        }

    }

    @Override
    public APIResponse<TouristGuideRequestDTO> getGuideDetails(Long id) {
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

        List<PolicySection> policySection=policySectionRepository.findByServices_ServiceId(id);
        List<PolicySectionRequest> policies=new ArrayList<>();

        //get the policies
        for (PolicySection policy:policySection){
            PolicySectionRequest guidePolicies=new PolicySectionRequest();
            guidePolicies.setId(policy.getId());
            guidePolicies.setHeading(policy.getHeading());
            guidePolicies.setPolicy(policy.getPolicy());
            policies.add(guidePolicies);
        }

        //Prepare the response

        TouristGuideRequestDTO prepareResponse=new TouristGuideRequestDTO();

        prepareResponse.setServiceName(touristGuide.getServiceName());
        prepareResponse.setContactNo(touristGuide.getContactNo());
        prepareResponse.setLocationBased(touristGuide.getLocationBased());
        prepareResponse.setPolicySection(policies);
        prepareResponse.setTabsSection(tabs);
//        prepareResponse.setLanguages(touristGuide.getLanguages());
        prepareResponse.setServiceAreas(touristGuide.getServiceAreas());


        return APIResponse.<TouristGuideRequestDTO>builder()
                .success(true)
                .message("Tourist Guide with ID: "+touristGuide.getServiceId())
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
        touristGuide.setServiceAreas(requestDTO.getServiceAreas());

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
