package com.lankatrails.lankatrails_backend.model;

import com.lankatrails.lankatrails_backend.model.enums.BudgetCategory;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "trip_budget_category")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TripBudgetCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "limit_id")
    private Long limitId;

    @Enumerated(EnumType.STRING)
    @Column(name = "budget_category", nullable = false, columnDefinition = "VARCHAR(20)")
    private BudgetCategory budgetCategory;

    @Column(name = "limit_amount", nullable = false)
    private Double limitAmount;

    @Column(name = "spent_amount", nullable = false)
    private Double spentAmount = 0.0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    public TripBudgetCategory(BudgetCategory category, Double limit, Trip trip) {
        this.budgetCategory = category;
        this.limitAmount = limit;
        this.trip = trip;
    }

}
