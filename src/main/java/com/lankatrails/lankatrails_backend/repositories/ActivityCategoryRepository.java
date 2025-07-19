package com.lankatrails.lankatrails_backend.repositories;

import com.lankatrails.lankatrails_backend.model.ActivityCategory;
import com.lankatrails.lankatrails_backend.model.enums.ActivityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivityCategoryRepository extends JpaRepository<ActivityCategory, Long> {
    ActivityCategory findByCategoryName(ActivityType categoryName);
}
