package com.lankatrails.lankatrails_backend.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransportRequestDTO extends ServiceRequest{
    private Integer vehicleCapacity;
    private Integer vehicleQty;
    private Double pricePerKm;
    private String vehicleCategory;
}
