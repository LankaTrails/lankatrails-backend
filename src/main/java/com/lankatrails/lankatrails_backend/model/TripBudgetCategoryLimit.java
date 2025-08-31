package com.lankatrails.lankatrails_backend.model;

import java.math.BigDecimal;

import com.lankatrails.lankatrails_backend.model.enums.BudgetCategory;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
