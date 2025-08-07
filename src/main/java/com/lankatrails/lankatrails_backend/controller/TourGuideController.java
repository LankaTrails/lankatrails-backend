package com.lankatrails.lankatrails_backend.controller;


import com.lankatrails.lankatrails_backend.dtos.request.PolicySectionRequest;
import com.lankatrails.lankatrails_backend.dtos.request.TouristGuideRequestDTO;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.TouristGuideResponseDTO;
import com.lankatrails.lankatrails_backend.model.PolicySection;
import com.lankatrails.lankatrails_backend.model.Provider;
import com.lankatrails.lankatrails_backend.repositories.TouristGuideRepository;
import com.lankatrails.lankatrails_backend.security.utils.AuthUtils;
import com.lankatrails.lankatrails_backend.service.ServicesForAll;
import com.lankatrails.lankatrails_backend.service.TouristGuideService;
import com.lankatrails.lankatrails_backend.service.impl.PolicyImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/provider")
public class TourGuideController {
    @Autowired
    TouristGuideService touristGuideService;

    @Autowired
    ServicesForAll servicesForAll;

    @Autowired
    AuthUtils authUtils;

    @Autowired
    PolicyImpl policyImpl;

    @GetMapping("/tour-guide/getAll")
    public ResponseEntity<APIResponse<TouristGuideResponseDTO>> getAllTourGuides
            (
                    @RequestParam(name = "pageNumber") Integer pageNumber,
                    @RequestParam(name = "pageSize") Integer pageSize
            ) {

        APIResponse<TouristGuideResponseDTO> touristGuideResponseDTO = touristGuideService.getAllTourGuides(pageNumber,pageSize);
        return new ResponseEntity<>(touristGuideResponseDTO, HttpStatus.OK);
    }

    @GetMapping("/tour-guide/{id}")
    public ResponseEntity<APIResponse<TouristGuideRequestDTO>> getGuideDetails(@PathVariable Long id) {
        APIResponse<TouristGuideRequestDTO> touristGuideResponseDTO = touristGuideService.searchWithId(id);
        return new ResponseEntity<>(touristGuideResponseDTO, HttpStatus.OK);
    }

    @PostMapping(value = "/tour-guide/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TouristGuideResponseDTO> addNewTourGuide
            (
                    @RequestPart("service") @Valid TouristGuideRequestDTO requestDTO,
                    @RequestPart(value = "images", required = false) List<MultipartFile> images
            ) {
        TouristGuideResponseDTO touristGuideRequestDTO = touristGuideService.addNewTouristGuide(requestDTO, images);

        return new ResponseEntity<>(touristGuideRequestDTO, HttpStatus.CREATED);
    }

    @GetMapping("/delete/{id}")
    public String deleteTourGuide(@PathVariable Long id) {
        Boolean removeStatus = servicesForAll.removeService(id);
        if (removeStatus)
            return "successfull";
        else
            return "unsuccessfull";

    }

    @PostMapping("/{id}")
    public ResponseEntity<TouristGuideResponseDTO> updateTourGuide(@PathVariable Long id, @RequestBody TouristGuideRequestDTO requestDTO) {
        TouristGuideResponseDTO touristGuideResponseDTO = touristGuideService.updateTourGuide(id, requestDTO);
        return new ResponseEntity<>(touristGuideResponseDTO, HttpStatus.OK);
    }

    @PostMapping("/policy/tour-guide")
    public ResponseEntity<APIResponse<String>> addPolicies(@RequestBody PolicySection policies){
        APIResponse<String> responseDTO= touristGuideService.addNewPolicy(policies);
        return new ResponseEntity<>(responseDTO,HttpStatus.CREATED);
    }
    @GetMapping("/policy/tour-guide")
    public ResponseEntity<APIResponse<List<PolicySectionRequest>>> guidePolicies (){
        Provider provider = (Provider) authUtils.loggedInUser();
        List<PolicySectionRequest> policies = policyImpl.getServicePolicies(provider.getUserId(),5L);
        APIResponse<List<PolicySectionRequest>> response =APIResponse.<List<PolicySectionRequest>>builder()
                .success(true)
                .message("Found TourGuide Policies")
                .data(policies)
                .build();

        return new ResponseEntity<>(response,HttpStatus.OK);

    }
}
