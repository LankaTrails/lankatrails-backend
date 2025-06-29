package com.lankatrails.lankatrails_backend.model;


import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tourist_guide")
@Getter
@Setter
@NoArgsConstructor
public class TouristGuide extends Services{
    private String serviceAreas;
    private String languages;

}
