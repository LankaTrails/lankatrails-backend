package com.lankatrails.lankatrails_backend.dtos;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPreferencesDTO {
    private String preferredCurrency = "LKR";
    private String timeZone = "Asia/Colombo";
    private String language = "en-LK";
    private Boolean is24Hour = true;
    private String measurementSystem = "metric";
    private Boolean notificationsEnabled = false;
    private Boolean emailNotifications = false;
    private Boolean pushNotifications = false;
    private Boolean smsNotifications = false;
}
