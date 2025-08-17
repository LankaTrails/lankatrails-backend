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
    private  ActivityCategory activityCategory;

    private String activityDetails;

    private String safetyInstructions;

    private String activityDuration;
}
