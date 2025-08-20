package com.lankatrails.lankatrails_backend.model;


import com.lankatrails.lankatrails_backend.model.enums.InvitationStatus;
import com.lankatrails.lankatrails_backend.model.enums.TripRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "trip_invitations")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TripInvitation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @Column(name = "token", nullable = false, unique = true)
    private String token;

    @Column(name = "role", nullable = false)
    private TripRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InvitationStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_expired", nullable = false)
    @Builder.Default
    private Boolean isExpired = false;

    @Column(name = "is_group_invitation", nullable = false)
    @Builder.Default
    private Boolean isGroupInvitation = false;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private Tourist createdBy;
}
