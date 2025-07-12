package com.lankatrails.lankatrails_backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tabs_section")
@Getter
@Setter
@NoArgsConstructor
public class TabsSection {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Size(max = 20)
    @NotBlank(message = "Heading cannot be empty")
    private String heading;

    @NotBlank(message = "Content cannot be empty")
    private String content;

    @ManyToOne
    @JoinColumn(name = "service_id")
    private Service service;

}
