package com.lankatrails.lankatrails_backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "accommodation_type")
@ToString
public class AccommodationCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "accommodation_type_id")
    private Integer accommodationTypeId;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, name = "name", nullable = false, unique = true, columnDefinition = "VARCHAR(20)")
    private com.lankatrails.lankatrails_backend.model.enums.AccommodationType categoryName;

    public AccommodationCategory(com.lankatrails.lankatrails_backend.model.enums.AccommodationType accommodationType) {
        this.categoryName = accommodationType;
    }
}
