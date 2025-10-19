package com.lankatrails.lankatrails_backend.controller;

import com.lankatrails.lankatrails_backend.dtos.request.LicenseDTO;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.BusinessResponseDTO;
import com.lankatrails.lankatrails_backend.dtos.response.LicenseResponseDTO;
import com.lankatrails.lankatrails_backend.service.ProviderService;
import io.vavr.API;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class LicenseController {
    @Autowired
    ProviderService providerService;

    @GetMapping("/admin/providers/unchecked")
    public ResponseEntity<APIResponse<BusinessResponseDTO>> loadProviders (){
        APIResponse<BusinessResponseDTO> businessDetailResponse = providerService.getBusinessDetails();
        return  new ResponseEntity<>(businessDetailResponse, HttpStatus.OK);
    }

    @GetMapping("/admin/licenses/unchecked")
    public ResponseEntity<APIResponse<LicenseResponseDTO>> loadAllLicenses(){
        APIResponse<LicenseResponseDTO> response = providerService.getLicenses();
        return  new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/admin/license/{id}")
    public ResponseEntity<APIResponse<LicenseResponseDTO>> loadLicenseDetails(@PathVariable Long id){
        APIResponse<LicenseResponseDTO> response = providerService.getLicense(id);
        return  new ResponseEntity<>(response,HttpStatus.OK);
    }


}
