package com.lankatrails.lankatrails_backend.model;


import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tourist_guide")
@Getter
@Setter
@NoArgsConstructor
public class TouristGuide extends Service {
    private String serviceAreas;

    @OneToMany(mappedBy = "touristGuide")
    private Set<Language> languages = new HashSet<>();

}
