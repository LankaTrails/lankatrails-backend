package com.lankatrails.lankatrails_backend.dtos.request;

import com.lankatrails.lankatrails_backend.model.enums.FuelType;
import com.lankatrails.lankatrails_backend.model.enums.TransmissionType;
import com.lankatrails.lankatrails_backend.model.enums.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransportRequestDTO extends ServiceRequest {
    private VehicleType vehicleCategory;
    private Boolean driverIncluded;
    private Boolean airConditioned;
    private TransmissionType transmissionType;
    private FuelType fuelType;
}
