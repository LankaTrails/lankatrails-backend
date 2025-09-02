package com.lankatrails.lankatrails_backend.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import com.lankatrails.lankatrails_backend.model.enums.BookingStatus;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @Column(name = "total_price", scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "paid_amount", scale = 2)
    private BigDecimal paidAmount;

    @Column(name = "deposit_amount", scale = 2)
    private BigDecimal depositAmount;
    
    @Enumerated(EnumType.STRING)
    private BookingStatus bookingStatus;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Payment> payments = new java.util.HashSet<>();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
