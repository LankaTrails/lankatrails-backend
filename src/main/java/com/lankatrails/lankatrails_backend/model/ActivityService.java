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
public class ActivityService extends Services{
    private  String activity_type;

    private String activity_details;

    private String safety_instructions;


}
