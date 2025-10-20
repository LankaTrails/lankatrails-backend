package com.lankatrails.lankatrails_backend.model;

import com.lankatrails.lankatrails_backend.model.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "external_bookings")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExternalBooking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingId;

    @OneToOne
    @JoinColumn(name = "trip_item_id")
    private Service tripItem;

    @ManyToOne
    @JoinColumn(name = "service_id")
    private Service service;

    @Column(name = "booked_date_time")
    private LocalDateTime bookedDateTime;

    @Column(name = "start_date_time")
    private LocalDateTime startDateTime;

    @Column(name = "end_date_time")
    private LocalDateTime endDateTime;

    @Column(name = "total_price", scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "paid_amount", scale = 2)
    private BigDecimal paidAmount;

    @Column(name = "deposit_amount", scale = 2)
    private BigDecimal depositAmount;

    @Enumerated(EnumType.STRING)
    private BookingStatus bookingStatus;
}
