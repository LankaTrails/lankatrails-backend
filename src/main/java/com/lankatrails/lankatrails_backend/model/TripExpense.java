package com.lankatrails.lankatrails_backend.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
@Table(name = "trip_expenses")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TripExpense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "expense_id")
    private Long expenseId;

    @Column(name = "expense_name", nullable = false)
    private String expenseName;

    @Column(name = "amount", nullable = false, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, columnDefinition = "VARCHAR(20)")
    private BudgetCategory budgetCategory;

    @Column(name = "expense_date_time", nullable = false)
    private LocalDateTime expenseDateTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paid_by_id", nullable = false)
    private Tourist paidBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

}
