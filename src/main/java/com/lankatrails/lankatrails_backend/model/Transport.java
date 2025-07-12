package com.lankatrails.lankatrails_backend.model;

import jakarta.persistence.Entity;
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

    private String vehicleType;
    private Integer vehicleQty;
}
