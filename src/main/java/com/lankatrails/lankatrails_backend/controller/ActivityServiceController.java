package com.lankatrails.lankatrails_backend.controller;

import com.lankatrails.lankatrails_backend.dtos.request.ActivityServiceRequest;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.ActivityServiceResponse;
import com.lankatrails.lankatrails_backend.model.ActivityService;
import com.lankatrails.lankatrails_backend.model.PolicySection;
import com.lankatrails.lankatrails_backend.model.TabsSection;
import com.lankatrails.lankatrails_backend.service.ActivityServiceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ActivityServiceController {
    @Autowired
    ActivityServiceService activityServiceService;

    @PostMapping("/provider/activity-service/add")
    public ResponseEntity<APIResponse<String>> addService
            (
                  @Valid @RequestBody ActivityServiceRequest service,
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
                   APIResponse<String> ActivityServiceDTO =  activityServiceService.addService(service);
                   return new ResponseEntity<>(ActivityServiceDTO,HttpStatus.CREATED);
               }

    }
    @GetMapping("activity-service/delete/{id}")
    public ResponseEntity<APIResponse<ActivityServiceRequest>> removeActivityService(@PathVariable Long id){
        APIResponse<ActivityServiceRequest> activityServiceResponse= activityServiceService.removeActivityService(id);
        return new ResponseEntity<>(activityServiceResponse,HttpStatus.OK);
    }

    @GetMapping("/activity-service/getAll")
    public ResponseEntity<APIResponse<ActivityServiceResponse>> getAll_ActivityServices(
            @RequestParam(name = "pageNumber") Integer pageNumber,
            @RequestParam(name = "pageSize") Integer pageSize
    ){

        APIResponse<ActivityServiceResponse> activityServiceResponse= activityServiceService.getAll_ActivityServices(pageNumber,pageSize);
        return new ResponseEntity<>(activityServiceResponse,HttpStatus.OK);
    }

    @GetMapping("activity-service/{id}")
    public ResponseEntity<APIResponse<ActivityServiceRequest>> searchById(@PathVariable Long id){
        APIResponse<ActivityServiceRequest> activityService= activityServiceService.searchWithId(id);
        return new ResponseEntity<>(activityService,HttpStatus.OK);
    }


    @PutMapping("/provider/activity-service/update/{id}")
    public ResponseEntity<APIResponse<String>> updateActivityService
            (
             @RequestBody ActivityServiceRequest activityService,
             @PathVariable Long id
            ){
        APIResponse<String> updatedService= activityServiceService.updateWithId(id,activityService);
        return  new ResponseEntity<>(updatedService,HttpStatus.OK);
    }

    //For Tabs
    @PostMapping("/{id}/tabs")
    public ResponseEntity<ActivityServiceRequest> addTab
            (
                    @PathVariable Long id,
                    @RequestBody TabsSection tabsSection
            ){

        ActivityServiceRequest addTabs= activityServiceService.addTabs(id,tabsSection);
        return new ResponseEntity<>(addTabs,HttpStatus.CREATED);

    }

    @GetMapping("/delete/tabs/{id}")
    public ResponseEntity<APIResponse<String>> removeTabs(@PathVariable Long id){
        APIResponse<String> response= activityServiceService.removeTabs(id);
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    //For Policies
    @PostMapping("/{id}/policies")
    public ResponseEntity<APIResponse<String>> addPolicies(@PathVariable Long id, @RequestBody PolicySection policies){
        APIResponse<String> responseDTO= activityServiceService.addNewPolicy(id,policies);
        return new ResponseEntity<>(responseDTO,HttpStatus.CREATED);
    }

    @GetMapping("/delete/policy/{id}")
    public ResponseEntity<APIResponse<String>> removePolicy(@PathVariable Long id){
        APIResponse<String> response= activityServiceService.removePolicies(id);
        return new ResponseEntity<>(response,HttpStatus.OK);
    }













}
