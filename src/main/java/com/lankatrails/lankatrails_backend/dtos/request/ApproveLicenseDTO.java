package com.lankatrails.lankatrails_backend.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApproveLicenseDTO {
    private List<LicenseDTO> accommodation;
    private List<LicenseDTO> activity;
    private List<LicenseDTO> transport;
    private List<LicenseDTO> tourGuide;
    private List<LicenseDTO> foodBeverage;
}
