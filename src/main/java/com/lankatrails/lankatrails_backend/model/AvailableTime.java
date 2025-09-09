package com.lankatrails.lankatrails_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "available_times")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvailableTime {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long availableTimeId;

    @Column(name = "day_of_week")
    private String dayOfWeek;

    @Column(name = "open_time")
    private LocalTime openTime;

    @Column(name = "close_time")
    private LocalTime closeTime;

    @Column(name = "is_24_hours")
    private Boolean is24Hours;

    @Column(name = "is_closed")
    private Boolean isClosed;

    @ManyToOne
    @JoinColumn(name = "service_id")
    private Service service;

    @OneToMany(mappedBy = "availableTime", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BreakTime> breakTimes = new ArrayList<>();
}
