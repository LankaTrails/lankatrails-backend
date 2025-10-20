package com.lankatrails.lankatrails_backend.dtos.request;

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
public class ProviderInfoDTO {
    private Long providerId;
    private String businessName;
    private BusinessType businessType;
    private String businessRegistrationNumber;
    private UserStatus status;
    private String email;
    private String city;
}
