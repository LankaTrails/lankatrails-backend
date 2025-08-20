package com.lankatrails.lankatrails_backend.model;

import com.lankatrails.lankatrails_backend.model.enums.TripPrivilege;
import com.lankatrails.lankatrails_backend.model.enums.TripRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "trip_participants")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TripParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long participantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tourist_id", nullable = false)
    private Tourist tourist;

    @Enumerated(EnumType.STRING)
    @Column(name = "trip_role", nullable = false, columnDefinition = "VARCHAR(20)")
    private TripRole tripRole = TripRole.MEMBER;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_by_tourist_id", referencedColumnName = "user_id")
    private Tourist invitedBy = null;

    @ElementCollection(targetClass = TripPrivilege.class, fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "trip_participant_privileges", joinColumns = @JoinColumn(name = "participant_id"))
    @Column(name = "privilege")
    private Set<TripPrivilege> privileges = new HashSet<>();

}
