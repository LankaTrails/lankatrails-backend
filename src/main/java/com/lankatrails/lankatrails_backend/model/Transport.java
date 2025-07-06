package com.lankatrails.lankatrails_backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.units.qual.N;

@Entity
@Table(name = "transport")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Transport extends Services{

    private String vehicleType;
    private Integer vehicleQty;
}
