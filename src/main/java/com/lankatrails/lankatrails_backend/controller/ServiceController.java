package com.lankatrails.lankatrails_backend.controller;

import com.lankatrails.lankatrails_backend.dtos.request.ProviderDetailsRequest;
import com.lankatrails.lankatrails_backend.dtos.request.ServiceSearchRequestDTO;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.ProviderDetailsDTO;
import com.lankatrails.lankatrails_backend.dtos.response.SearchResponseDTO;
import com.lankatrails.lankatrails_backend.model.enums.ServiceCategory;
import com.lankatrails.lankatrails_backend.service.ServiceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/service")
public class ServiceController {
    @Autowired
    ServiceService serviceService;

    @PostMapping("/search")
    public ResponseEntity<APIResponse<SearchResponseDTO>> searchServices(
            @RequestBody ServiceSearchRequestDTO requestDTO
    ) {
        APIResponse<SearchResponseDTO> response = serviceService.searchServicesAdvanced(requestDTO);
        return ResponseEntity.status(HttpStatus.OK)
                .body(response);
    }

    @GetMapping("/provider/{providerId}/{category}")
    public ResponseEntity<APIResponse<ProviderDetailsDTO>> getServicesByProviderAndCategory(
            @PathVariable Long providerId,
            @PathVariable ServiceCategory category
    ) {
        APIResponse<ProviderDetailsDTO> response = serviceService.getServicesByProviderAndCategory(providerId, category);
        return ResponseEntity.status(HttpStatus.OK)
                .body(response);
    }

    @PostMapping("/provider")
    public ResponseEntity<APIResponse<ProviderDetailsDTO>> getServicesByProviderAndCategory(
            @Valid @RequestBody ProviderDetailsRequest request
    ) {
        APIResponse<ProviderDetailsDTO> response = serviceService.getServicesByProviderAndCategory(request);
        return ResponseEntity.status(HttpStatus.OK)
                .body(response);
    }

}
