package com.lankatrails.lankatrails_backend.dtos.request;

import com.lankatrails.lankatrails_backend.model.enums.ApprovalStatus;
import com.lankatrails.lankatrails_backend.model.enums.BusinessType;
import com.lankatrails.lankatrails_backend.model.enums.UserRole;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Getter
@Setter
public class ProviderRegistrationRequest extends RegistrationRequest {
    @Size(max = 50)
    private String businessName;

    private String businessDescription;

    private BusinessType businessType;

    private LocationDTO location;

    private String coverImageUrl;

    private String businessRegistrationNumber;

    private String businessRegistrationUrl;

    private ApprovalStatus accommodationApprovalStatus;

    private ApprovalStatus tourGuideApprovalStatus;

    private ApprovalStatus transportApprovalStatus;

    private ApprovalStatus activityApprovalStatus;

    private ApprovalStatus foodApprovalStatus;

    private ContactPersonDTO contactPerson;

    private List<LicenseDTO> licenses;

}