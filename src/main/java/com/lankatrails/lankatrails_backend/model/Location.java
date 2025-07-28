package com.lankatrails.lankatrails_backend.model;

import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "location")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "location_id")
    private Long locationId;

    @Column(name = "formatted_address", nullable = false)
    private String formattedAddress;

    @Column(name = "city", columnDefinition = "TEXT")
    private String city;

    @Column(name = "district", columnDefinition = "TEXT")
    private String district;

    @Column(name = "province", columnDefinition = "TEXT")
    private String province;

    @Column(name = "country", columnDefinition = "TEXT")
    private String country;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "coordinates", columnDefinition = "geography(Point,4326)")
    private Point coordinates;

    @OneToMany(mappedBy = "locationBased",cascade = CascadeType.ALL)
    private Set<Service> services = new HashSet<>();

    @ManyToMany(mappedBy = "locations", fetch = FetchType.LAZY)
    private Set<Trip> trips;

    //Auto-set coordinates before saving or updating
    @PrePersist
    @PreUpdate
    private void setPointFromLatLng() {
        if (latitude != null && longitude != null) {
            GeometryFactory geometryFactory = new GeometryFactory();
            this.coordinates = geometryFactory.createPoint(new Coordinate(longitude, latitude));
            this.coordinates.setSRID(4326);
        }
    }

}
