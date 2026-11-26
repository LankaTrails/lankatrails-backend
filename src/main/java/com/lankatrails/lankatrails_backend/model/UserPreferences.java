package com.lankatrails.lankatrails_backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_preferences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPreferences {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "preferred_currency", length = 10)
    @Builder.Default
    private String preferredCurrency = "LKR";

    @Column(name = "time_zone", length = 50)
    @Builder.Default
    private String timeZone = "Asia/Colombo";

    @Column(name = "language", length = 20)
    @Builder.Default
    private String language = "en-LK";

    @Column(name = "is_24_hour")
    @Builder.Default
    private Boolean is24Hour = true;

    @Column(name = "measurement_system", length = 20)
    @Builder.Default
    private String measurementSystem = "metric";

    @Column(name = "notifications_enabled")
    @Builder.Default
    private Boolean notificationsEnabled = true;

    @Column(name = "email_notifications")
    @Builder.Default
    private Boolean emailNotifications = true;

    @Column(name = "push_notifications")
    @Builder.Default
    private Boolean pushNotifications = true;

    @Column(name = "sms_notifications")
    @Builder.Default
    private Boolean smsNotifications = false;
}

