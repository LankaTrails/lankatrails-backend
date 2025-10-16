package com.lankatrails.lankatrails_backend.repositories;

import com.lankatrails.lankatrails_backend.model.TripExpenseShare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.lankatrails.lankatrails_backend.model.TripExpense;

@Repository
public interface TripExpenseShareRepository extends JpaRepository<TripExpenseShare, Long> {

    // Custom query methods can be defined here if needed
    // For example, to find all shares for a specific expense:
    // List<TripExpenseShare> findByExpenseId(Long expenseId);

    // Or to find all shares for a specific participant:
    // List<TripExpenseShare> findByParticipantId(Long participantId);
    void deleteByTripExpense(TripExpense tripExpense);
}
