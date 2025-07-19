package com.lankatrails.lankatrails_backend.repositories;

import com.lankatrails.lankatrails_backend.model.VehicleCategory;
import com.lankatrails.lankatrails_backend.model.enums.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VehicleCategoryRepository extends JpaRepository<VehicleCategory, Long> {
    VehicleCategory findByCategoryName(VehicleType categoryName);
}
