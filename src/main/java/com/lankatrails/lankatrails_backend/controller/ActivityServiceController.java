package com.lankatrails.lankatrails_backend.controller;

import com.lankatrails.lankatrails_backend.dtos.request.ActivityServiceRequest;
import com.lankatrails.lankatrails_backend.dtos.request.TabSectionRequest;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.ActivityServiceResponse;
import com.lankatrails.lankatrails_backend.model.ActivityService;
import com.lankatrails.lankatrails_backend.model.PolicySection;
import com.lankatrails.lankatrails_backend.model.Services;
import com.lankatrails.lankatrails_backend.model.TabsSection;
import com.lankatrails.lankatrails_backend.service.ServicesService;
import io.vavr.API;
import jakarta.validation.Valid;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ActivityServiceController {
    @Autowired
    ServicesService servicesService;



    @PostMapping("/provider/activity-service/add")
    public ResponseEntity<ActivityServiceResponse> addService
            (
                    @RequestBody ActivityServiceRequest service
            ){
               ActivityServiceResponse ActivityServiceDTO =  servicesService.addService(service);
               //return ResponseEntity.status(HttpStatus.CREATED).body(ActivityServiceDTO);
               return new ResponseEntity<>(ActivityServiceDTO,HttpStatus.CREATED);
    }
    @GetMapping("/delete/{id}")
    public ResponseEntity<ActivityServiceRequest> removeActivityService(@PathVariable Long id,@Valid @RequestBody ActivityService activityService){
        ActivityServiceRequest activityServiceResponse=servicesService.removeActivityService(id,activityService);
        return new ResponseEntity<>(activityServiceResponse,HttpStatus.OK);
    }

    @GetMapping("/getAll")
    public ResponseEntity<ActivityServiceResponse> getAll_ActivityServices(
            @RequestParam(name = "pageNumber") Integer pageNumber,
            @RequestParam(name = "pageSize") Integer pageSize
    ){

        ActivityServiceResponse activityServiceResponse=servicesService.getAll_ActivityServices(pageNumber,pageSize);
        return new ResponseEntity<>(activityServiceResponse,HttpStatus.OK);
    }

    @GetMapping("activity-service/{id}")
    public ResponseEntity<ActivityServiceRequest> searchById(@PathVariable Long id){
        ActivityServiceRequest activityService=servicesService.searchWithId(id);
        return new ResponseEntity<>(activityService,HttpStatus.OK);
    }

    @PostMapping("/update/{id}")
    public ResponseEntity<ActivityServiceRequest> updateActivityService
            (
             @RequestBody ActivityServiceRequest activityService,
             @PathVariable Long id
            ){
        ActivityServiceRequest updatedService=servicesService.updateWithId(id,activityService);
        return  new ResponseEntity<>(updatedService,HttpStatus.OK);
    }

    //For Tabs
    @PostMapping("/{id}/tabs")
    public ResponseEntity<ActivityServiceRequest> addTab
            (
                    @PathVariable Long id,
                    @RequestBody TabsSection tabsSection
            ){

        ActivityServiceRequest addTabs=servicesService.addTabs(id,tabsSection);
        return new ResponseEntity<>(addTabs,HttpStatus.CREATED);

    }

    @GetMapping("/delete/tabs/{id}")
    public ResponseEntity<APIResponse<String>> removeTabs(@PathVariable Long id){
        APIResponse<String> response=servicesService.removeTabs(id);
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    //For Policies
    @PostMapping("/{id}/policies")
    public ResponseEntity<ActivityServiceRequest> addPolicies(@PathVariable Long id, @RequestBody PolicySection policies){
        ActivityServiceRequest responseDTO=servicesService.addNewPolicy(id,policies);
        return new ResponseEntity<>(responseDTO,HttpStatus.CREATED);
    }

    @GetMapping("/delete/policy/{id}")
    public ResponseEntity<APIResponse<String>> removePolicy(@PathVariable Long id){
        APIResponse<String> response=servicesService.removePolicies(id);
        return new ResponseEntity<>(response,HttpStatus.OK);
    }













}
