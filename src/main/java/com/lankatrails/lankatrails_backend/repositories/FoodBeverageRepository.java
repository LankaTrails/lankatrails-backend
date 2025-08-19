package com.lankatrails.lankatrails_backend.repositories;

import com.lankatrails.lankatrails_backend.model.FoodAndBeverage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FoodBeverageRepository extends JpaRepository<FoodAndBeverage,Long> {
    Optional<FoodAndBeverage> findByServiceName(String serviceName);
    Optional<FoodAndBeverage> findByServiceId(Long id);
}
