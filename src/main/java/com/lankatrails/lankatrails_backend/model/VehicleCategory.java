package com.lankatrails.lankatrails_backend.model;

import com.lankatrails.lankatrails_backend.model.enums.VehicleType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "vehicle_category")
@Getter
@Setter
@NoArgsConstructor
public class VehicleCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long vehicleCategoryId;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, name = "name", nullable = false, unique = true, columnDefinition = "VARCHAR(20)")
    private VehicleType categoryName;

    @OneToMany(mappedBy = "vehicleCategory")
    private Set<Transport> transports = new HashSet<>();

    public VehicleCategory(VehicleType categoryName) {
        this.categoryName = categoryName;
    }

}
