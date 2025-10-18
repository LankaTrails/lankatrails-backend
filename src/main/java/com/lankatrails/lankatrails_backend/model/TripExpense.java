package com.lankatrails.lankatrails_backend.model;

import com.lankatrails.lankatrails_backend.model.enums.BudgetCategory;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @Column(name = "total_expense_amount", nullable = false)
    private Double totalExpenseAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_participant_id", nullable = false)
    private TripParticipant createdByParticipant;

    @OneToMany(mappedBy = "tripExpense", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<TripExpenseShare> shares;

}
