package com.lankatrails.lankatrails_backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Entity
@Getter
@Setter
@Table(name = "availability_slots")
@NoArgsConstructor
public class AvailableTime {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long slotId;

    @Column(name = "day_of_week")
    private String dayOfWeek;

    @Column(name = "open_time")
    private LocalTime openTime;

    @Column(name = "close_time")
    private LocalTime closeTime;

    @ManyToOne
    @JoinColumn(name = "service_id")
    private Service service;
}
