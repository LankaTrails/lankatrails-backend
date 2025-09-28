package com.lankatrails.lankatrails_backend.model;

import com.lankatrails.lankatrails_backend.model.enums.TourGuideType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "tour_guide_category")
@ToString
public class TourGuideCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long tourGuideCategoryId;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, name = "name", nullable = false, unique = true, columnDefinition = "VARCHAR(20)")
    private TourGuideType categoryName;

    @OneToMany(mappedBy = "tourGuideCategory")
    private Set<TouristGuide> touristGuides;

    public TourGuideCategory(TourGuideType categoryName) {
        this.categoryName = categoryName;
    }
}
