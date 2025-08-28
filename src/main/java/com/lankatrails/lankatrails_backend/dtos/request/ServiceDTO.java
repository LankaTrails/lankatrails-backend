package com.lankatrails.lankatrails_backend.dtos.request;

import com.lankatrails.lankatrails_backend.dtos.response.ProviderProfileDto;
import com.lankatrails.lankatrails_backend.model.Image;
import com.lankatrails.lankatrails_backend.model.Location;
import com.lankatrails.lankatrails_backend.model.enums.PriceType;
import com.lankatrails.lankatrails_backend.model.enums.ServiceCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ServiceDTO {
    private Long serviceId;
    private String serviceName;
    private ServiceCategory Category;
    private Set<LocationDTO> locations;
    private Double price;
    private PriceType priceType;
    private String mainImageUrl;

//    private List<AvailabilitySlotDTO> availabilitySlots;

}

