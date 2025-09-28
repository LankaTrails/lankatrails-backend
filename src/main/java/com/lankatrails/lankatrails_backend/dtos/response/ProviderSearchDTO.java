package com.lankatrails.lankatrails_backend.dtos.response;

import com.lankatrails.lankatrails_backend.dtos.request.LocationDTO;
import com.lankatrails.lankatrails_backend.model.enums.ServiceCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProviderSearchDTO {
    private Long providerId;
    private String businessName;
    private String coverImageUrl;
    private ServiceCategory Category;
    private LocationDTO location;
//    private List<ServiceDTO> services;
}

