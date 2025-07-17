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
public class ActivityService extends Services {
    private  String activityType;

    private String activityDetails;

    private String safetyInstructions;


}
