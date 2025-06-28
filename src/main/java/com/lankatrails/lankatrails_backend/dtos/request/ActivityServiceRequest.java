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
public class ActivityServiceRequest extends ServiceRequest {
    private String activityType;
    private String activityDetails;
    private String safetyInstructions;
    private List<TabSectionRequest> tabsSection;
    private List<PolicySectionRequest> policySection;


}
