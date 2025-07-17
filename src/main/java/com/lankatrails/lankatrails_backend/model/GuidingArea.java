package com.lankatrails.lankatrails_backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "service_area")
@Getter
@Setter
@NoArgsConstructor
public class GuidingArea {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long serviceAreaId;

    private String serviceArea;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "touristGuide_id")
    private TouristGuide touristGuide;


}
