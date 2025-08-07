package com.lankatrails.lankatrails_backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class AvailabilitySlot {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long slotId;
    private String dayOfWeek;

    private String openTime;
    private String closeTime;

    @ManyToOne
    @JoinColumn(name = "service_id")
    private Service service;
}
