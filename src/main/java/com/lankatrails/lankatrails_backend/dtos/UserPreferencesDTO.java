package com.lankatrails.lankatrails_backend.dtos;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPreferencesDTO {
    @Builder.Default
    private String preferredCurrency = "LKR";
    @Builder.Default
    private String timeZone = "Asia/Colombo";
    @Builder.Default
    private String language = "en-LK";
    @Builder.Default
    private Boolean is24Hour = true;
    @Builder.Default
    private String measurementSystem = "metric";
    @Builder.Default
    private Boolean notificationsEnabled = false;
    @Builder.Default
    private Boolean emailNotifications = false;
    @Builder.Default
    private Boolean pushNotifications = false;
    @Builder.Default
    private Boolean smsNotifications = false;
}
