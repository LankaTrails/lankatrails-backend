package com.lankatrails.lankatrails_backend.dtos.response;

import com.lankatrails.lankatrails_backend.dtos.request.LocationDTO;
import com.lankatrails.lankatrails_backend.model.enums.PriceType;
import com.lankatrails.lankatrails_backend.model.enums.ServiceCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ServiceSearchDTO {
    private Long serviceId;
    private String serviceName;
    private ServiceCategory Category;
    private LocationDTO locationBased;
    private Double price;
    private PriceType priceType;
    private String mainImageUrl;
}
