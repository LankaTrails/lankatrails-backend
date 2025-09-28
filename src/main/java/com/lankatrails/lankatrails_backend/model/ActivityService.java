package com.lankatrails.lankatrails_backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "activity_services")
@Getter
@Setter
@NoArgsConstructor
public class ActivityService extends Service {

    @ManyToOne
    @JoinColumn(name = "activityCategory_id")
    private ActivityCategory activityCategory;

    @Column(name = "activity_details", length = 2000)
    private String activityDetails;

    @Column(name = "safety_instructions", length = 2000)
    private String safetyInstructions;

}
