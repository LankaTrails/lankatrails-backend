package com.lankatrails.lankatrails_backend.controller;

import com.lankatrails.lankatrails_backend.dtos.request.AccommodationServiceRequestDTO;
import com.lankatrails.lankatrails_backend.dtos.request.PolicySectionRequest;
import com.lankatrails.lankatrails_backend.dtos.request.TransportRequestDTO;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.TransportResponseDTO;
import com.lankatrails.lankatrails_backend.model.PolicySection;
import com.lankatrails.lankatrails_backend.model.Provider;
import com.lankatrails.lankatrails_backend.security.utils.AuthUtils;
import com.lankatrails.lankatrails_backend.service.TransportService;
import com.lankatrails.lankatrails_backend.service.impl.PolicyImpl;
import io.vavr.API;
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
@RequestMapping("api/provider")
public class TransportController {
    @Autowired
    TransportService transportService;

    @Autowired
    AuthUtils authUtils;

    @Autowired
    PolicyImpl policyImpl;

    @GetMapping("/transport/getAll")
    public ResponseEntity<APIResponse<TransportResponseDTO>> getAllTourists(
            @RequestParam(name = "pageNumber") Integer pageNumber,
            @RequestParam(name = "pageSize") Integer pageSize
    ){
        APIResponse<TransportResponseDTO> transportResponseDTO=transportService.getAll(pageNumber,pageSize);
        return  new ResponseEntity<>(transportResponseDTO, HttpStatus.OK);

    }
    @GetMapping("/transport/{id}")
    public ResponseEntity<APIResponse<TransportRequestDTO>> searchWithId(@PathVariable Long id){
        APIResponse<TransportRequestDTO> transportResponseDTO=transportService.getById(id);
        return new ResponseEntity<>(transportResponseDTO,HttpStatus.OK);
    }
    @PutMapping("/transport/{id}")
    public ResponseEntity<TransportResponseDTO> updateTransport(
            @PathVariable Long id,
            @RequestBody TransportRequestDTO transportRequestDTO
            ){
        TransportResponseDTO transportResponseDTO=transportService.updateTransport(id,transportRequestDTO);
        return new ResponseEntity<>(transportResponseDTO,HttpStatus.OK);
    }
    @PostMapping(value = "/transport/add",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<APIResponse<String>> addNewTransport(
            @RequestPart("service") @Valid  TransportRequestDTO transportRequestDTO,
            @RequestPart(value = "images", required = false)List<MultipartFile> images,
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
            return  new ResponseEntity<>(errorResponse,HttpStatus.BAD_REQUEST);
        }else{
            APIResponse<String> transportResponseDTO=transportService.addNewTransport(transportRequestDTO,images);
            return new ResponseEntity<>(transportResponseDTO,HttpStatus.OK);
        }


    }
    @PutMapping("/transport/remove/{id}")
    public ResponseEntity<APIResponse<String>> deleteTransport(@PathVariable Long id){
        APIResponse<String> response=transportService.deleteTransport(id);
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    @PostMapping("/policy/transport")
    public ResponseEntity<APIResponse<String>> addPolicies(@RequestBody PolicySection policies){
        APIResponse<String> responseDTO= transportService.addNewPolicy(policies);
        return new ResponseEntity<>(responseDTO,HttpStatus.CREATED);
    }
    @GetMapping("/policy/transport")
    public ResponseEntity<APIResponse<List<PolicySectionRequest>>> guidePolicies (){
        List<PolicySectionRequest> policies = policyImpl.getServicePolicies(authUtils.loggedInUserId(), 2L);
        APIResponse<List<PolicySectionRequest>> response =APIResponse.<List<PolicySectionRequest>>builder()
                .success(true)
                .message("Found Transport Policies")
                .data(policies)
                .build();

        return new ResponseEntity<>(response,HttpStatus.OK);

    }

    @PutMapping(value = "/transport/update/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<APIResponse<String>> updateTransport(
            @PathVariable Long id,
            @RequestPart("service") @Valid TransportRequestDTO transportRequestDTO,
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
            return  new ResponseEntity<>(errorResponse,HttpStatus.BAD_REQUEST);
        }else{
            APIResponse<String> transportResponseDTO=transportService.updateTransport(id,transportRequestDTO,images);
            return new ResponseEntity<>(transportResponseDTO,HttpStatus.OK);
        }
    }

}
