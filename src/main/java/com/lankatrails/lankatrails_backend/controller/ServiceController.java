package com.lankatrails.lankatrails_backend.controller;

import com.lankatrails.lankatrails_backend.dtos.request.ServiceDTO;
import com.lankatrails.lankatrails_backend.dtos.request.ServiceSearchRequestDTO;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.GroupedServiceDTO;
import com.lankatrails.lankatrails_backend.service.ServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/service")
public class ServiceController {
    @Autowired
    ServiceService serviceService;

    @PostMapping("/search")
    public ResponseEntity<APIResponse<List<GroupedServiceDTO>>> searchServices(
            @RequestBody ServiceSearchRequestDTO requestDTO
    ) {
        APIResponse<List<GroupedServiceDTO>> response = serviceService.searchServicesAdvanced(requestDTO);
        return ResponseEntity.status(HttpStatus.OK)
                .body(response);
    }

}
