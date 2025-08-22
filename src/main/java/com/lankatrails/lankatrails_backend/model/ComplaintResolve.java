package com.lankatrails.lankatrails_backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintResolve {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long resolveId;

    private String name;

    @OneToMany(mappedBy = "complaintResolve")
    private List<Complaint> complaints;
}
