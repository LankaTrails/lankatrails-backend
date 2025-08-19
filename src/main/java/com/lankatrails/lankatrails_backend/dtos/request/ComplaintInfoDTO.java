package com.lankatrails.lankatrails_backend.dtos.request;

import com.lankatrails.lankatrails_backend.model.enums.BusinessType;
import com.lankatrails.lankatrails_backend.model.enums.ComplaintStatus;
import com.lankatrails.lankatrails_backend.model.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintInfoDTO {
    private String businessName;
    private BusinessType businessType;
    private String touristEmail;
    private UserStatus userStatus;
    private ComplaintStatus complaintStatus;
}
