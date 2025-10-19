package com.lankatrails.lankatrails_backend.dtos.response;

import com.lankatrails.lankatrails_backend.dtos.request.LicenseDTO;
import com.lankatrails.lankatrails_backend.model.enums.ApprovalStatus;
import com.lankatrails.lankatrails_backend.model.enums.ServiceCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LicenseResponse {
    private List<LicenseDTO> licenses;
    private ApprovalStatus approvalStatus;
    private ServiceCategory serviceCategory;
}
