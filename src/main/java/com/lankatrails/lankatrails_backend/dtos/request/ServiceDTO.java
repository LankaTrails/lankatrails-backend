package com.lankatrails.lankatrails_backend.dtos.request;

import com.lankatrails.lankatrails_backend.dtos.response.ProviderProfileDto;
import com.lankatrails.lankatrails_backend.model.Image;
import com.lankatrails.lankatrails_backend.model.Location;
import com.lankatrails.lankatrails_backend.model.enums.PriceType;
import com.lankatrails.lankatrails_backend.model.enums.ServiceCategory;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ServiceDTO {
    private Long serviceId;
    private String serviceName;
    private ServiceCategory Category;
    private LocationDTO locationBased;
    private Double price;
    private PriceType priceType;
    private String mainImageUrl;

    private List<AvailabilitySlotDTO> availabilitySlots;

}

