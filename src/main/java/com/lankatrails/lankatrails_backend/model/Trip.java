package com.lankatrails.lankatrails_backend.model;

import com.lankatrails.lankatrails_backend.model.enums.TripStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
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

    @Column(name = "trip_name")
    private String tripName;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "start_location_id", referencedColumnName ="location_id" )
    private Location startLocation;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "trip_locations",
            joinColumns = @JoinColumn(name = "trip_id"),
            inverseJoinColumns = @JoinColumn(name = "location_id"))
    private List<Location> locations = new ArrayList<>();

    @Column(name = "number_of_adults", nullable = false)
    private Integer numberOfAdults = 1;

    @Column(name = "number_of_children", nullable = false)
    private Integer numberOfChildren = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "trip_status", nullable = false, columnDefinition = "VARCHAR(20)")
    private TripStatus tripStatus;

    @Column(name = "total_budget")
    private Double totalSpentAmount = 0.0;

    @Column(name = "total_budget_limit")
    private Double totalBudgetLimit = 0.0;

    @Column(name = "total_distance")
    private Double totalDistance = 0.0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lead_tourist_id", referencedColumnName = "user_id")
    private Tourist leadTourist;

    @OneToOne(mappedBy = "trip", fetch = FetchType.LAZY)
    private ChatRoom chatRoom;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "trip_tourists",
            joinColumns = @JoinColumn(name = "trip_id"),
            inverseJoinColumns = @JoinColumn(name = "tourist_id"))
    private Set<Tourist> tourists = new HashSet<>();

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("startTime ASC")
    private List<TripItem> tripItems;

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TripExpense> tripExpenses;

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<TripBudgetCategory> tripBudgetCategories;
}
