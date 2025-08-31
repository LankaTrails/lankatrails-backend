package com.lankatrails.lankatrails_backend.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lankatrails.lankatrails_backend.model.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Getter
@Setter
@Table(name = "bookings")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingId;

    @OneToOne
    @JoinColumn(name = "trip_item_id")
    private TripItem tripItem;

    @ManyToOne
    @JoinColumn(name = "trip_participant_id")
    private TripParticipant tripParticipant;

    @Column(name = "booked_date_time")
    private LocalDateTime bookedDateTime;

    @Column(name = "start_date_time")
    private LocalDateTime startDateTime;

    @Column(name = "end_date_time")
    private LocalDateTime endDateTime;

    @Column(name = "total_price")
    private Double totalPrice;

    @Column(name = "paid_amount")
    private Double paidAmount;

    @Column(name = "deposit_amount")
    private Double depositAmount;

    @Enumerated(EnumType.STRING)
    private BookingStatus bookingStatus;
}
