package com.lankatrails.lankatrails_backend.model;

import com.lankatrails.lankatrails_backend.model.enums.ActivityType;
import com.lankatrails.lankatrails_backend.model.enums.VehicleType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "activity_category")
@ToString
public class ActivityCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "activity_category_id")
    private Long categoryId;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, name = "name", nullable = false, unique = true, columnDefinition = "VARCHAR(20)")
    private ActivityType categoryName;

    @OneToMany(mappedBy = "activityCategory")
    private Set<ActivityService> activityServices;

    public ActivityCategory(ActivityType categoryName) {
        this.categoryName = categoryName;
    }
}
