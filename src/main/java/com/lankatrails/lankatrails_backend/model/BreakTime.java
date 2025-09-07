package com.lankatrails.lankatrails_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Entity
@Getter
@Setter
@Table(name = "break_times")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BreakTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long breakId;

    @Column(name = "break_start")
    private LocalTime breakStart;

    @Column(name = "break_end")
    private LocalTime breakEnd;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "available_time_id")
    private AvailableTime availableTime;
}
