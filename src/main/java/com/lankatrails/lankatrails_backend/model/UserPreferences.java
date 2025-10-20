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
    private String preferredCurrency = "LKR";

    @Column(name = "time_zone", length = 50)
    private String timeZone = "Asia/Colombo";

    @Column(name = "language", length = 20)
    private String language = "en-LK";

    @Column(name = "is_24_hour")
    private Boolean is24Hour = true;

    @Column(name = "measurement_system", length = 20)
    private String measurementSystem = "metric";

    @Column(name = "notifications_enabled")
    private Boolean notificationsEnabled = true;

    @Column(name = "email_notifications")
    private Boolean emailNotifications = true;

    @Column(name = "push_notifications")
    private Boolean pushNotifications = true;

    @Column(name = "sms_notifications")
    private Boolean smsNotifications = false;
}

