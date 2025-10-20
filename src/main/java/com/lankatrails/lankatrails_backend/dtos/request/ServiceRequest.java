package com.lankatrails.lankatrails_backend.dtos.request;

import com.lankatrails.lankatrails_backend.model.enums.ServiceStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class ServiceRequest {
    private Long serviceId;

    @NotBlank(message = "Service name is blank")
    private String serviceName;

    @NotBlank(message = "Contact number is blank")
    @Pattern(
            regexp = "^(\\+94|0)(7[0-9]{8}|[1-9][0-9]{8})$",
            message = "Invalid contact number format"
    )
    private String contactNo;

    private ServiceStatus status;

    private List<TabSectionRequest> tabsSection;

    private List<PolicySectionRequest> policySection;

    private Set<LocationDTO> locations;

    private List<ImageRequestDTO> images;

    private List<AvailableTimeDTO> availableTimeDTOS;

    private List<ImageRequestDTO> deletedImages;

    private List<TabSectionRequest> deletedTabs;

    private List<PolicySectionRequest> deletedPolicies;

    private PriceConfigDTO priceConfig;

    private BookingConfigDTO bookingConfig;

    private Double averageRating;

    private Long reviewCount;

    private Long futureBookingCount;

    private Long pastBookingCount;

}
