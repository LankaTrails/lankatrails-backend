package com.lankatrails.lankatrails_backend.repositories;

import com.lankatrails.lankatrails_backend.model.TripBudgetCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TripBudgetCategoryRepository extends JpaRepository<TripBudgetCategory, Long> {
    // Custom query methods can be defined here if needed
}
