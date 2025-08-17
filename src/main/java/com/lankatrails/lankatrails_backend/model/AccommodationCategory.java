package com.lankatrails.lankatrails_backend.model;

import com.lankatrails.lankatrails_backend.model.enums.AccommodationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "accommodation_type")
@ToString
public class AccommodationCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Integer accommodationCategoryId;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, name = "name", nullable = false, unique = true, columnDefinition = "VARCHAR(20)")
    private AccommodationType categoryName;

    @OneToMany(mappedBy = "accommodationCategory")
    private Set<Accommodation> accommodations = new HashSet<>();

    public AccommodationCategory(com.lankatrails.lankatrails_backend.model.enums.AccommodationType accommodationType) {
        this.categoryName = accommodationType;
    }
}
