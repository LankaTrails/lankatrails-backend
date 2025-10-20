package com.lankatrails.lankatrails_backend.repositories;

import com.lankatrails.lankatrails_backend.model.TripExpense;
import com.lankatrails.lankatrails_backend.model.enums.BudgetCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TripExpenseRepository extends JpaRepository<TripExpense, Long> {
    
    /**
     * Find all expenses for a specific trip
     */
    List<TripExpense> findByTrip_TripId(Long tripId);
    
    /**
     * Find expenses by trip and budget category
     */
    List<TripExpense> findByTrip_TripIdAndBudgetCategory(Long tripId, BudgetCategory budgetCategory);
    
    /**
     * Find expenses created by a specific participant
     */
    List<TripExpense> findByCreatedByParticipant_ParticipantId(Long participantId);
    
    /**
     * Get total expenses for a trip
     */
    @Query("SELECT COALESCE(SUM(e.totalExpenseAmount), 0.0) FROM TripExpense e WHERE e.trip.tripId = :tripId")
    Double getTotalExpensesByTripId(@Param("tripId") Long tripId);
    
    /**
     * Get total expenses for a trip by category
     */
    @Query("SELECT COALESCE(SUM(e.totalExpenseAmount), 0.0) FROM TripExpense e WHERE e.trip.tripId = :tripId AND e.budgetCategory = :category")
    Double getTotalExpensesByTripIdAndCategory(@Param("tripId") Long tripId, @Param("category") BudgetCategory category);
    
    /**
     * Delete all expenses for a specific trip
     */
    void deleteByTrip_TripId(Long tripId);
}
