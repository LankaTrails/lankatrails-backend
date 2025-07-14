package com.lankatrails.lankatrails_backend.dtos.request;

import com.lankatrails.lankatrails_backend.model.Category;
import com.lankatrails.lankatrails_backend.model.Image;
import com.lankatrails.lankatrails_backend.model.Location;
import com.lankatrails.lankatrails_backend.model.Provider;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class ServiceRequest {
    private Long serviceId;

    @NotBlank(message = "Service name is blank")
    private String serviceName;

//    @NotBlank(message = "Location is not pinned")
    private Location locationBased;

    @NotBlank(message = "Contact number is blank")
    @Pattern(regexp = "^(\\+94|0)?7[0-9]{8}$", message = "Invalid contact number format")
    private String contactNo;

    private Boolean status;

    private List<TabSectionRequest> tabsSection;

//    @NotBlank(message = "Cancellation Policy is required")
    private List<PolicySectionRequest> policySection;

//    private List<PolicySectionRequest> providerPolicies;


//    @Valid
//    @NotEmpty(message = "Should be at least one image")
    private List<ImageRequestDTO> images;




}
