package com.lankatrails.lankatrails_backend.model;

import com.lankatrails.lankatrails_backend.model.enums.TripStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
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
    @JoinColumn(name = "start_location_id", referencedColumnName = "location_id")
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

    @Column(name = "total_budget", scale = 2)
    private BigDecimal totalBudget = BigDecimal.ZERO;

    @Column(name = "total_budget_limit", scale = 2)
    private BigDecimal totalBudgetLimit = BigDecimal.ZERO;

    @Column(name = "total_distance")
    private Double totalDistance = 0.0;

    @OneToOne(mappedBy = "trip", fetch = FetchType.LAZY)
    private GroupChatRoom chatRoom;

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<TripParticipant> participants = new HashSet<>();

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("startTime ASC")
    private List<TripItem> tripItems;

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TripExpense> tripExpenses;

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<TripBudgetCategoryLimit> tripBudgetCategoryLimits;
}
