package com.lankatrails.lankatrails_backend.repositories;

import com.lankatrails.lankatrails_backend.model.FoodAndBeverageCategory;
import com.lankatrails.lankatrails_backend.model.enums.FoodAndBeverageType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FoodAndBeverageCategoryRepository extends JpaRepository<FoodAndBeverageCategory, Long> {
    Optional<FoodAndBeverageCategory> findByCategoryName(FoodAndBeverageType categoryName);
}
