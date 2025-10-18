package com.lankatrails.lankatrails_backend.model;

import com.lankatrails.lankatrails_backend.model.enums.ComplaintResult;
import com.lankatrails.lankatrails_backend.model.enums.ComplaintStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Complaint {
    @Id
    @GeneratedValue
    private Long complaintId;

    private String description;

    @Enumerated(EnumType.STRING)
    private ComplaintStatus complaintStatus;

    @Enumerated(EnumType.STRING)
    private ComplaintResult complaintResult;

    private LocalDateTime dateTime;

    private String investigationStartedDate;

    @ManyToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @ManyToOne
    @JoinColumn(name = "service_id")
    private Service service;

    @ManyToOne
    @JoinColumn(name = "tourist_id")
    private Tourist tourist;

    @OneToMany(mappedBy = "complaint")
    private List<ComplaintImage> complaintImages;

    @ManyToOne
    @JoinColumn(name = "resolve_id")
    private ComplaintResolve complaintResolve;

    @ManyToOne
    @JoinColumn(name = "reject_id")
    private ComplaintReject complaintReject;

}
