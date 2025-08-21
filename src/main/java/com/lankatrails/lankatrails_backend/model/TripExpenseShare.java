package com.lankatrails.lankatrails_backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "trip_expense_shares")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TripExpenseShare {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "share_id")
    private Long shareId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_id", nullable = false)
    private TripExpense tripExpense;

    @Column(name = "amount", nullable = false)
    private Double amount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", nullable = false)
    private TripParticipant tripParticipant;
}
