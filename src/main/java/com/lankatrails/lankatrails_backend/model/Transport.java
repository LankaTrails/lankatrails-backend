package com.lankatrails.lankatrails_backend.model;

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
    private Double pricePerKm;

    @ManyToOne
    @JoinColumn(name = "vehicleCategory_id")
    private VehicleCategory vehicleCategory;
}
