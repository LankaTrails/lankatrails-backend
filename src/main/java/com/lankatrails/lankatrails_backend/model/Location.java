package com.lankatrails.lankatrails_backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "location")
@Getter
@Setter
public class Location {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long locationId;
    private String formattedAddress;
    private String city;
    private String district;
    private String province;
    private String country;
    private String postalCode;
    private Double latitude;
    private Double longitude;




}
