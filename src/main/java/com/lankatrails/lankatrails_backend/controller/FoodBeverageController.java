package com.lankatrails.lankatrails_backend.controller;

import com.lankatrails.lankatrails_backend.dtos.request.AccommodationServiceRequestDTO;
import com.lankatrails.lankatrails_backend.dtos.request.ActivityServiceRequest;
import com.lankatrails.lankatrails_backend.dtos.request.FoodBeverageRequest;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.ActivityServiceResponse;
import com.lankatrails.lankatrails_backend.dtos.response.FoodBeverageResponse;
import com.lankatrails.lankatrails_backend.service.FoodBeverageService;
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
public class FoodBeverageController {
    @Autowired
    FoodBeverageService foodBeverageService;

    @PostMapping(value = "/food-beverage/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<APIResponse<String>> addService
            (
                    @RequestPart("service") @Valid FoodBeverageRequest service,
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
            APIResponse<String> foodBeverageDTO = foodBeverageService .addService(service, images);
            return new ResponseEntity<>(foodBeverageDTO,HttpStatus.CREATED);
        }

    }

    @GetMapping("/food-beverage/getAll")
    public ResponseEntity<APIResponse<FoodBeverageResponse>> getAll(
            @RequestParam(name = "pageNumber") Integer pageNumber,
            @RequestParam(name = "pageSize") Integer pageSize
    ){

        APIResponse<FoodBeverageResponse> foodBeverageResponse= foodBeverageService.getAll(pageNumber,pageSize);
        return new ResponseEntity<>(foodBeverageResponse,HttpStatus.OK);
    }

    @GetMapping("/food-beverage/{Id}")
    public ResponseEntity<APIResponse<FoodBeverageRequest>> searchById(@PathVariable Long Id){
        APIResponse<FoodBeverageRequest> accommodationResponse = foodBeverageService.searchWithId(Id);
        return new ResponseEntity<>(accommodationResponse,HttpStatus.OK);
    }
}
