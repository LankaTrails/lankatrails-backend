package com.lankatrails.lankatrails_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
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

    @Column(name = "number_of_people")
    private Integer numberOfPeople = 1;

    @Column(name = "total_budget")
    private Double totalBudget = 0.0;

    @Column(name = "total_budget_limit")
    private Double totalBudgetLimit = 0.0;

    @Column(name = "total_distance")
    private Double totalDistance = 0.0;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lead_tourist_id", referencedColumnName = "user_id")
    private Tourist leadTourist;

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
    private Set<TripBudgetCategoryLimit> tripBudgetCategoryLimits;
}
