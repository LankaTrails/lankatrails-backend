package com.lankatrails.lankatrails_backend.repositories;

import com.lankatrails.lankatrails_backend.model.TourGuideCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TourGuideCategoryRepository extends JpaRepository<TourGuideCategory, Long> {
    TourGuideCategory findByCategoryName(String categoryName);
}
