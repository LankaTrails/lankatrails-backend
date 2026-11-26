package com.lankatrails.lankatrails_backend.controller;

import com.lankatrails.lankatrails_backend.service.ProviderInsightsService;
import com.lankatrails.lankatrails_backend.model.enums.ServiceCategory;
import com.lankatrails.lankatrails_backend.dtos.request.ProviderProfileUpdateRequest;
import com.lankatrails.lankatrails_backend.dtos.response.ProviderInsightsDTO;
import com.lankatrails.lankatrails_backend.dtos.request.ContactPersonDTO;
import com.lankatrails.lankatrails_backend.dtos.request.LicenseDTO;
import com.lankatrails.lankatrails_backend.dtos.request.PolicySectionRequest;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.BusinessResponseDTO;
import com.lankatrails.lankatrails_backend.dtos.response.LicenseResponse;
import com.lankatrails.lankatrails_backend.model.Provider;
import com.lankatrails.lankatrails_backend.repositories.ProviderRepository;
import com.lankatrails.lankatrails_backend.security.utils.AuthUtils;
import com.lankatrails.lankatrails_backend.service.ProviderService;
import com.lankatrails.lankatrails_backend.service.impl.PolicyImpl;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/provider")
public class ProviderController {
    @Autowired
    PolicyImpl policyImplementation;

    @Autowired
    AuthUtils authUtils;

    @Autowired
    ProviderService providerService;

    @Autowired
    ProviderRepository providerRepository;

    @Autowired
    ProviderInsightsService providerInsightsService;

    @PostMapping("/add/policy")
    public ResponseEntity<APIResponse<String>> addPublicPolicy
            (@RequestBody PolicySectionRequest policyReq) {
        Provider provider = providerRepository.findById(authUtils.loggedInUserId())
                .orElseThrow(() -> new RuntimeException("Provider not found with id: " + authUtils.loggedInUserId()));
        policyReq.setProvider(provider);
        APIResponse<String> response = policyImplementation.providerAddPolicies(policyReq);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/policies")
    public ResponseEntity<APIResponse<List<PolicySectionRequest>>> providerPolicies() {
        List<PolicySectionRequest> policies = policyImplementation.getProviderPolicies(authUtils.loggedInUserId());
        APIResponse<List<PolicySectionRequest>> response = APIResponse.<List<PolicySectionRequest>>builder()
                .success(true)
                .message("Found Provider Policies")
                .data(policies)
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);

    }

    //Policies of the provider and also the policies common to a single service
    @GetMapping("/policies/{categoryId}")
    public ResponseEntity<APIResponse<List<PolicySectionRequest>>> providerAndServicePolicies(@PathVariable Long categoryId) {
        List<PolicySectionRequest> policies = policyImplementation
                .getProviderAndServicePolicies(authUtils.loggedInUserId(), categoryId);
        APIResponse<List<PolicySectionRequest>> response = APIResponse.<List<PolicySectionRequest>>builder()
                .success(true)
                .message("Found Provider and Service Policies")
                .data(policies)
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);

    }

    //    @GetMapping("/business-details")
//    public ResponseEntity<APIResponse<BusinessDetailDTO>> getBusinessDetails() {
//        APIResponse<BusinessDetailDTO> response = providerService.getBusinessDetails();
//        return ResponseEntity.status(HttpStatus.OK)
//                .body(response);
//    }
    @GetMapping("/provider-details")
    public ResponseEntity<APIResponse<BusinessResponseDTO>> getProviderDetails() {
        APIResponse<BusinessResponseDTO> response = providerService.getBusinessDetails();
        return new ResponseEntity<>(response, HttpStatus.OK);

    }

    @PreAuthorize("@ownership.isPolicyOwner(#id)")
    @DeleteMapping("/delete/policy/{id}")
    public ResponseEntity<APIResponse<String>> removePolicy(@PathVariable Long id) {
        APIResponse<String> response = policyImplementation.removePolicies(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // Updates the logged-in provider's contact person; the identity document is optional
    @PutMapping(value = "/contact-person", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<APIResponse<ContactPersonDTO>> updateContactPerson(
            @RequestPart("contactPerson") ContactPersonDTO contactPerson,
            @RequestPart(value = "identityDocument", required = false) MultipartFile identityDocument) {
        APIResponse<ContactPersonDTO> response = providerService.updateContactPerson(contactPerson, identityDocument);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /** Totals, revenue, rating, 6-month trend and recent activity for the dashboard. */
    @GetMapping("/insights")
    public ResponseEntity<APIResponse<ProviderInsightsDTO>> getInsights(
            @RequestParam(value = "category", required = false) ServiceCategory category) {
        ProviderInsightsDTO insights = providerInsightsService.getInsights(authUtils.loggedInUserId(), category);
        return ResponseEntity.ok(APIResponse.<ProviderInsightsDTO>builder()
                .success(true)
                .message("Provider insights")
                .data(insights)
                .build());
    }

    @PutMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<APIResponse<String>> updateProfile(
            @RequestPart("profile") @Valid ProviderProfileUpdateRequest profile,
            @RequestPart(value = "profilePicture", required = false) MultipartFile profilePicture,
            @RequestPart(value = "coverImage", required = false) MultipartFile coverImage) {
        return ResponseEntity.ok(providerService.updateProfile(profile, profilePicture, coverImage));
    }

    @PostMapping(value = "/licenses", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<APIResponse<String>> renewLicense(
            @RequestPart("license") @Valid List<LicenseDTO> licenseDTO,
            @RequestPart(value = "licenseFiles", required = false) List<MultipartFile> licenseFiles) {

        APIResponse<String> response = providerService.licenseRenewal(licenseDTO, licenseFiles);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping(value = "/approval", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<APIResponse<String>> requestApproval(
            @RequestPart("license") @Valid List<LicenseDTO> licenseDTO,
            @RequestPart(value = "licenseFiles", required = false) List<MultipartFile> licenseFiles) {

        APIResponse<String> response = providerService.requestApproval(licenseDTO, licenseFiles);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/licenses")
    public ResponseEntity<APIResponse<Set<LicenseResponse>>> getLicenseDetails() {
        APIResponse<Set<LicenseResponse>> response = providerService.getLicenseForProvider();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


}
