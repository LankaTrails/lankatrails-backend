package com.lankatrails.lankatrails_backend.repositories;

import com.lankatrails.lankatrails_backend.model.FoodAndBeverage;
import com.lankatrails.lankatrails_backend.model.FoodAndBeverageCategory;
import com.lankatrails.lankatrails_backend.model.Transport;
import com.lankatrails.lankatrails_backend.model.enums.FoodAndBeverageType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FoodAndBeverageCategoryRepository extends JpaRepository<FoodAndBeverageCategory, Long> {
    Optional<FoodAndBeverageCategory> findByCategoryName(FoodAndBeverageType categoryName);

}
