package com.lankatrails.lankatrails_backend.controller;


import com.lankatrails.lankatrails_backend.dtos.request.FoodBeverageRequest;
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
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        List<PolicySectionRequest> policies = policyImpl.getServicePolicies(authUtils.loggedInUserId(), 5L);
        APIResponse<List<PolicySectionRequest>> response =APIResponse.<List<PolicySectionRequest>>builder()
                .success(true)
                .message("Found TourGuide Policies")
                .data(policies)
                .build();

        return new ResponseEntity<>(response,HttpStatus.OK);

    }

    @PutMapping(value = "/tour-guide/update/{Id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<APIResponse<String>> updateAccommodation
            (
                    @PathVariable Long Id,
                    @RequestPart("service") @Valid TouristGuideRequestDTO service,
                    @RequestPart(value = "images", required = false) List<MultipartFile> images,
                    BindingResult result
            ){
        if (result.hasErrors()){
            Map<String,String> errors = new HashMap<>();
            result.getFieldErrors().forEach(field ->{
                errors.put(field.getField(), field.getDefaultMessage());
            });
            APIResponse<String> errorResponse = APIResponse.<String>builder()
                    .success(false)
                    .message("Validation Failed")
                    .details(errors)
                    .build();
            return  new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }else{
            APIResponse<String> ActivityServiceDTO =  touristGuideService.updateService(Id, service, images);
            return new ResponseEntity<>(ActivityServiceDTO,HttpStatus.OK);
        }

    }

    @PutMapping("/tour-guide/remove/{Id}")
    public ResponseEntity<APIResponse<String>> deleteAccommodation(@PathVariable Long Id) {
        APIResponse<String> response = touristGuideService.deleteService(Id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
