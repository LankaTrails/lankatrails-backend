package com.lankatrails.lankatrails_backend.model;


import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tourist_guide")
@Getter
@Setter
@NoArgsConstructor
public class TouristGuide extends Service {
    @OneToMany(mappedBy = "touristGuide")
    private Set<Language> languages = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "tour_guide_category_id")
    private TourGuideCategory tourGuideCategory;

    private Duration duration;
}
