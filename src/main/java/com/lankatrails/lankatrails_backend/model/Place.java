package com.lankatrails.lankatrails_backend.model;

import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "place")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Place {
    @Id
    @Column(name = "place_id")
    private String placeId;

    @Column(name = "place_name", nullable = false)
    private String placeName;

    @Column(name = "photo_reference", nullable = false)
    private String photoReference;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Column(name = "coordinates", columnDefinition = "geography(Point,4326)")
    private Point coordinates;

    @OneToMany(mappedBy = "place", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<TripItem> tripItems = new HashSet<>();

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
