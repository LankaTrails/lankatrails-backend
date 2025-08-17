package com.lankatrails.lankatrails_backend.repositories;

import com.lankatrails.lankatrails_backend.model.TripExpense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TripExpenseRepository extends JpaRepository<TripExpense, Long> {
    
    @Query("SELECT te FROM TripExpense te WHERE te.trip.tripId = :tripId")
    List<TripExpense> findByTripId(@Param("tripId") Long tripId);
    
    @Query("SELECT te FROM TripExpense te WHERE te.trip.tripId = :tripId AND te.budgetCategory = :category")
    List<TripExpense> findByTripIdAndCategory(@Param("tripId") Long tripId, @Param("category") String category);
    
    @Query("SELECT te FROM TripExpense te WHERE te.paidBy.userId = :userId")
    List<TripExpense> findByPaidByUserId(@Param("userId") Long userId);
}
