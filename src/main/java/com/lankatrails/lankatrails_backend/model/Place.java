package com.lankatrails.lankatrails_backend.model;

import jakarta.persistence.*;
import lombok.*;

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

    @OneToMany(mappedBy = "place", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<TripItem> tripItems = new HashSet<>();

}
