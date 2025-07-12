package com.lankatrails.lankatrails_backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

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

//    @ManyToOne
//    @JoinColumn(name = "service_id")
//    private Services service;

    @ManyToMany(mappedBy = "policies")
    private Set<Services> services = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "provider_id")
    private Provider provider;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;


}
