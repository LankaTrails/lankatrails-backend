package com.lankatrails.lankatrails_backend.dtos.request;

import com.lankatrails.lankatrails_backend.model.Category;
import com.lankatrails.lankatrails_backend.model.Location;
import com.lankatrails.lankatrails_backend.model.Provider;
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
    private String serviceName;
    private Location locationBased;
    private String contactNo;
    private Boolean status;
    private List<TabSectionRequest> tabsSection;
    private List<PolicySectionRequest> policySection;


}
