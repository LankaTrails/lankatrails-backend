package com.lankatrails.lankatrails_backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "contact_persons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactPerson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contact_person_id")
    private Long contactPersonId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "position", nullable = false)
    private String position;

    @Column(name = "identity_document_url")
    private String identityDocumentUrl;

}
