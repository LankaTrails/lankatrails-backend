package com.lankatrails.lankatrails_backend.dtos.request;

import com.lankatrails.lankatrails_backend.model.enums.VehicleType;
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
//    private Double pricePerKm;
    private VehicleType vehicleCategory;
}
