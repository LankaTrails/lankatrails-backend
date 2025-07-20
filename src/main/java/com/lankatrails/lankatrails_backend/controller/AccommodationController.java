package com.lankatrails.lankatrails_backend.controller;

import com.lankatrails.lankatrails_backend.dtos.request.AccommodationServiceRequestDTO;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.AccommodationResponse;
import com.lankatrails.lankatrails_backend.service.AccommodationService;
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
@RequestMapping("/api")
public class AccommodationController {

    @Autowired
    AccommodationService accommodationService;

    @PostMapping(value = "/provider/accommodation/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<APIResponse<String>> addTransport
            (
                    @RequestPart("service") @Valid AccommodationServiceRequestDTO service,
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
            APIResponse<String> ActivityServiceDTO =  accommodationService.addService(service, images);
            return new ResponseEntity<>(ActivityServiceDTO,HttpStatus.CREATED);
        }

    }

    @GetMapping("/provider/accommodation/getAll")
    public ResponseEntity<APIResponse<AccommodationResponse>> getAll_ActivityServices(
            @RequestParam(name = "pageNumber") Integer pageNumber,
            @RequestParam(name = "pageSize") Integer pageSize
    ){

        APIResponse<AccommodationResponse> accommodationResponse= accommodationService.getAll_Accommodations(pageNumber,pageSize);
        return new ResponseEntity<>(accommodationResponse,HttpStatus.OK);
    }

    @GetMapping("/provider/accommodation/{Id}")
    public ResponseEntity<APIResponse<AccommodationServiceRequestDTO>> searchById(@PathVariable Long Id){
        APIResponse<AccommodationServiceRequestDTO> accommodationResponse = accommodationService.searchWithId(Id);
        return new ResponseEntity<>(accommodationResponse,HttpStatus.OK);
    }

}
