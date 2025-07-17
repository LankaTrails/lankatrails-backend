package com.lankatrails.lankatrails_backend.repositories;

import com.lankatrails.lankatrails_backend.model.VehicleCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleCategoryRepository extends JpaRepository<VehicleCategory, Long> {
    VehicleCategory findByCategoryName(String categoryName);
}
