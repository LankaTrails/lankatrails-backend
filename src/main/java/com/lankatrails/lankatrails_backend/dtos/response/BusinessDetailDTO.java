package com.lankatrails.lankatrails_backend.dtos.response;

import com.lankatrails.lankatrails_backend.dtos.request.ContactPersonDTO;
import com.lankatrails.lankatrails_backend.model.enums.BusinessType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BusinessDetailDTO {
    private Long providerId;
    private BusinessType businessType;
    private String businessRegistrationNumber;
    private String businessRegistrationUrl;
    private ContactPersonDTO contactPerson;
}
