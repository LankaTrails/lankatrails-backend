package com.lankatrails.lankatrails_backend.model;

import com.lankatrails.lankatrails_backend.model.enums.BudgetCategory;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

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

    @Column(name = "budget_category", nullable = false)
    private BudgetCategory budgetCategory;

    @Column(name = "limit_amount", nullable = false)
    private Double limitAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

}
