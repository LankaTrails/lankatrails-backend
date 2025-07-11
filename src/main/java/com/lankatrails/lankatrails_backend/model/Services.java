package com.lankatrails.lankatrails_backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "services")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Services {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long serviceId;

    private String serviceName;

    private String contactNo;

    private Boolean status;

    @ManyToOne
    @JoinColumn(name = "provider_id")
    private Provider provider;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private  Category category;

    @OneToMany(mappedBy = "service")
    private Set<TabsSection> tabs=new HashSet<>();

    @OneToMany(mappedBy = "service")
    private Set<PolicySection> policies=new HashSet<>();

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "location_id", referencedColumnName ="locationId" )
    private Location locationBased;

    @OneToMany(mappedBy = "service" )
    private Set<Image> images = new HashSet<>();




}
