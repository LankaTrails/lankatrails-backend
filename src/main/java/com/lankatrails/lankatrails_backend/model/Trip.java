package com.lankatrails.lankatrails_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "trips")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Trip {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trip_id")
    private Long tripId;

    @Column(name = "trip_name", nullable = false)
    private String tripName;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "number_of_people", nullable = false)
    private Integer numberOfPeople = 1;

    @Column(name = "total_cost", nullable = false)
    private Double totalCost = 0.0;

    @Column(name = "total_distance", nullable = false)
    private Double totalDistance = 0.0;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lead_tourist_id", referencedColumnName = "user_id")
    private Tourist leadTourist;

    @ManyToMany(mappedBy = "trips", fetch = FetchType.LAZY)
    private Set<Tourist> tourists;

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("startTime ASC")
    private List<TripItem> tripItems;
}
