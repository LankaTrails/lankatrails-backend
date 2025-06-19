package com.lankatrails.lankatrails_backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

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
    private Integer service_id;

    private String service_name;

    private String location_based;

    private String contact_no;

    @ManyToOne
    @JoinColumn(name = "provider_id")
    private Provider provider;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private  Category category;

}
