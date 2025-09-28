package com.lankatrails.lankatrails_backend.model;

import com.lankatrails.lankatrails_backend.model.enums.BudgetCategory;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "trip_budget_category_limits")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TripBudgetCategoryLimit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "limit_id")
    private Long limitId;

    @Enumerated(EnumType.STRING)
    @Column(name = "budget_category", nullable = false, columnDefinition = "VARCHAR(20)")
    private BudgetCategory budgetCategory;

    @Column(name = "limit_amount", nullable = false, scale = 2)
    private BigDecimal limitAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    public TripBudgetCategoryLimit(BudgetCategory category, BigDecimal limit, Trip trip) {
        this.budgetCategory = category;
        this.limitAmount = limit;
        this.trip = trip;
    }

}
