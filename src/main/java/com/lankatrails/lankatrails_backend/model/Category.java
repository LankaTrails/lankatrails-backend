package com.lankatrails.lankatrails_backend.model;

import com.lankatrails.lankatrails_backend.model.enums.ServiceCategory;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "categories")
@ToString
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Integer categoryId;

    @ToString.Exclude
    @Enumerated(EnumType.STRING)
    @Column(length = 20, name = "name", nullable = false, unique = true, columnDefinition = "VARCHAR(20)")
    private ServiceCategory categoryName;

    @OneToMany(mappedBy = "category",cascade = CascadeType.ALL)
    private Set<Services> services = new HashSet<>();

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private Set<License> licenses = new HashSet<>();

    public Category(ServiceCategory categoryName) {
        this.categoryName = categoryName;
    }

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private Set<PolicySection> policies;

}
