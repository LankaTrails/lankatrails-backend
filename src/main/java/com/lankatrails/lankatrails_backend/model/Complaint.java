package com.lankatrails.lankatrails_backend.model;

import com.lankatrails.lankatrails_backend.model.enums.ComplaintStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @ManyToOne
    @JoinColumn(name = "service_id")
    private Service service;

    @ManyToOne
    @JoinColumn(name = "tourist_id")
    private Tourist tourist;

    @OneToMany(mappedBy = "complaint")
    private List<ComplaintImage> complaintImages;

}
