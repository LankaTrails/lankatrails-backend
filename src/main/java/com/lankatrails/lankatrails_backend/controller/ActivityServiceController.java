package com.lankatrails.lankatrails_backend.controller;

import com.lankatrails.lankatrails_backend.dtos.request.ActivityServiceRequest;
import com.lankatrails.lankatrails_backend.dtos.request.TabSectionRequest;
import com.lankatrails.lankatrails_backend.dtos.response.ActivityServiceResponse;
import com.lankatrails.lankatrails_backend.model.ActivityService;
import com.lankatrails.lankatrails_backend.model.PolicySection;
import com.lankatrails.lankatrails_backend.model.Services;
import com.lankatrails.lankatrails_backend.model.TabsSection;
import com.lankatrails.lankatrails_backend.service.ServicesService;
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
    public ResponseEntity<ActivityServiceRequest> addService
            (
                    @RequestBody ActivityServiceRequest service
            ){
               ActivityServiceRequest ActivityServiceDTO =  servicesService.addService(service);
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
    public String removeTabs(@PathVariable Long id){
        Boolean status=servicesService.removeTabs(id);

        if (status==true){
            return "successfully deleted";
        }else{
            return "Couldn't Delete";
        }


    }

    //For Policies
    @PostMapping("/{id}/policies")
    public ResponseEntity<ActivityServiceRequest> addPolicies(@PathVariable Long id, @RequestBody PolicySection policies){
        ActivityServiceRequest responseDTO=servicesService.addNewPolicy(id,policies);
        return new ResponseEntity<>(responseDTO,HttpStatus.CREATED);
    }

    @GetMapping("/delete/policy/{id}")
    public String removePolicy(@PathVariable Long id){
        Boolean status=servicesService.removePolicies(id);
        if (status){
            return "Successfully Deleted";
        }else{
            return "Couldn't Delete";
        }

    }













}
