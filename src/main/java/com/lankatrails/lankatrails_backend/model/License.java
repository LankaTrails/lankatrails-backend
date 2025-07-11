package com.lankatrails.lankatrails_backend.model;

import com.lankatrails.lankatrails_backend.model.enums.ServiceCategory;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Entity
@Table(name = "licenses")
@Getter
@Setter
@NoArgsConstructor
public class License {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "license_id")
    private Long licenseId;

    @Column(name = "license_number", nullable = false)
    private String licenseNumber;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(name = "license_url", nullable = false)
    private String licenseUrl;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne
    @JoinColumn(name = "provider_id")
    @ToString.Exclude
    private Provider provider;
}
