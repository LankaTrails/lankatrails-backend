package com.lankatrails.lankatrails_backend.dtos.response;

import com.lankatrails.lankatrails_backend.dtos.request.LocationDTO;
import com.lankatrails.lankatrails_backend.model.enums.ApprovalStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class ProviderProfileDto extends UserProfileDto {
    private String businessName;
    private String businessDescription;
    private String coverImageUrl;
    private LocationDTO location;
    private ApprovalStatus accommodationApprovalStatus;
    private ApprovalStatus tourGuideApprovalStatus;
    private ApprovalStatus transportApprovalStatus;
    private ApprovalStatus activityApprovalStatus;
    private ApprovalStatus foodApprovalStatus;
}
