package com.lankatrails.lankatrails_backend.dtos.request;

import com.lankatrails.lankatrails_backend.model.enums.BusinessType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BusinessDetailDTO {
    private Long providerId;
    private BusinessType businessType;
    private String businessRegistrationNumber;
    private String businessRegistrationUrl;
    private ContactPersonDTO contactPerson;
    private String categoryName;
    private List<LicenseDTO> licenseDTOList;
}
