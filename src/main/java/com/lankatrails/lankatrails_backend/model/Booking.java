package com.lankatrails.lankatrails_backend.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lankatrails.lankatrails_backend.model.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingId;

    @ManyToOne
    @JoinColumn(name = "service_id")
    private Service service;

    @ManyToOne
    @JoinColumn(name = "tourist_id")
    private Tourist tourist;
    private Integer adults;
    private Integer children;
    private String bookedDateTime;

//    @Column(columnDefinition = "TIME")
//    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

//    @Column(columnDefinition = "TIME")
//    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;

    private LocalDate fromDate;
    private LocalDate toDate;

    @Enumerated(EnumType.STRING)
    private BookingStatus bookingStatus;
}
