package com.lankatrails.lankatrails_backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class ComplaintImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long complaintImgId;

    private String imageUrl;

    @ManyToOne
    @JoinColumn(name = "complaint_id")
    private Complaint complaint;
}
