package com.lankatrails.lankatrails_backend.dtos.request;

import com.lankatrails.lankatrails_backend.dtos.PriceDTO;
import com.lankatrails.lankatrails_backend.dtos.ProviderDto;
import com.lankatrails.lankatrails_backend.model.enums.ServiceCategory;
import lombok.*;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ServiceDTO {
    private Long serviceId;
    private String serviceName;
    private ServiceCategory Category;
    private Set<LocationDTO> locations;
    private List<PriceDTO> prices;
    private String mainImageUrl;
    private ProviderDto provider;
    private Double averageRating;
    private Long totalBookingsForPastMonth;

}

