package com.lankatrails.lankatrails_backend.repositories;

import com.lankatrails.lankatrails_backend.model.TourGuideCategory;
import com.lankatrails.lankatrails_backend.model.enums.TourGuideType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TourGuideCategoryRepository extends JpaRepository<TourGuideCategory, Long> {
    TourGuideCategory findByCategoryName(TourGuideType categoryName);
}
