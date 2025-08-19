package com.lankatrails.lankatrails_backend.repositories;

import com.lankatrails.lankatrails_backend.model.TripBudgetCategoryLimit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TripBudgetCategoryLimitRepository extends JpaRepository<TripBudgetCategoryLimit, Long> {
    // Custom query methods can be defined here if needed
}
