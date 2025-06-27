package com.lankatrails.lankatrails_backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "policy_section")
@Getter
@Setter
@NoArgsConstructor
public class PolicySection {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String heading;
    private String policy;

    @ManyToOne
    @JoinColumn(name = "service_id")
    private Services service;

}
