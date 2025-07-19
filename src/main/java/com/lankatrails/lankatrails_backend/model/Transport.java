package com.lankatrails.lankatrails_backend.model;

import com.lankatrails.lankatrails_backend.model.enums.FuelType;
import com.lankatrails.lankatrails_backend.model.enums.TransmissionType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "transport")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Transport extends Service {

    private Integer vehicleCapacity;
    private Integer vehicleQty;
    private Boolean driverIncluded;
    private Boolean airConditioned;
    private TransmissionType transmissionType;
    private FuelType fuelType;

    @ManyToOne
    @JoinColumn(name = "vehicleCategory_id")
    private VehicleCategory vehicleCategory;
}
