package com.lankatrails.lankatrails_backend.service.impl;

import com.lankatrails.lankatrails_backend.dtos.request.ActivityServiceRequest;
import com.lankatrails.lankatrails_backend.dtos.request.PolicySectionRequest;
import com.lankatrails.lankatrails_backend.dtos.request.TabSectionRequest;
import com.lankatrails.lankatrails_backend.dtos.request.TouristGuideRequestDTO;
import com.lankatrails.lankatrails_backend.dtos.response.TouristGuideResponseDTO;
import com.lankatrails.lankatrails_backend.exception.ResourceNotFoundException;
import com.lankatrails.lankatrails_backend.exception.ServiceAlreadyExistsException;
import com.lankatrails.lankatrails_backend.model.*;
import com.lankatrails.lankatrails_backend.model.enums.ServiceCategory;
import com.lankatrails.lankatrails_backend.repositories.CategoryRepository;
import com.lankatrails.lankatrails_backend.repositories.PolicySectionRepository;
import com.lankatrails.lankatrails_backend.repositories.TabsSectionRepository;
import com.lankatrails.lankatrails_backend.repositories.TouristGuideRepository;
import com.lankatrails.lankatrails_backend.security.utils.AuthUtils;
import com.lankatrails.lankatrails_backend.service.TouristGuideService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

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
    private ModelMapper modelMapper;

    @Autowired
    private AuthUtils authUtils;

    @Override
    public TouristGuideResponseDTO getAllTourGuides() {
        List<TouristGuide> allGuides=touristGuideRepository.findAll();
        List<TouristGuideRequestDTO> responseList=new ArrayList<>();
        for (TouristGuide guide : allGuides){
            TouristGuideRequestDTO response=new TouristGuideRequestDTO();
            response.setLanguages(guide.getLanguages());
            response.setServiceAreas(guide.getServiceAreas());
            response.setServiceName(guide.getServiceName());
            response.setContactNo(guide.getContactNo());
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
                    policySection.setService(lastGuideAdded);
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
}
