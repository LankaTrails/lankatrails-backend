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
    public static final int CONTENT_MAX_LENGTH = 20_000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(max = 20)
    @NotBlank(message = "Heading cannot be empty")
    private String heading;

    // Rich HTML from the dashboard editor; varchar(255) truncated real descriptions
    @NotBlank(message = "Content cannot be empty")
    @Size(max = TabsSection.CONTENT_MAX_LENGTH, message = "Content is too long")
    @Column(columnDefinition = "TEXT")
    private String content;

    @ManyToOne
    @JoinColumn(name = "service_id")
    private Service service;

}
