package com.lankatrails.lankatrails_backend.dtos.request;

import com.lankatrails.lankatrails_backend.dtos.response.ApproveLicenseResponse;
import com.lankatrails.lankatrails_backend.model.enums.BusinessType;
import com.lankatrails.lankatrails_backend.model.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProviderViewInfoDTO {
    private Long providerId;
    private String email;
    private String profilePicUrl;
    private UserStatus status;
    private String businessDescription;
    private String businessName;
    private String businessRegistrationNumber;
    private String businessRegistrationUrl;
    private BusinessType businessType;
    private String coverImgUrl;
    private ApproveLicenseResponse pendingLicenses;
    private ContactPersonDTO contactPerson;

}
