package com.lankatrails.lankatrails_backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Services {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer service_id;

    private String service_name;

    private String location_based;

    private String contact_no;

    @ManyToOne
    @JoinColumn(name = "provider_id")
    private Provider provider_id;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private  Category category_id;



}
