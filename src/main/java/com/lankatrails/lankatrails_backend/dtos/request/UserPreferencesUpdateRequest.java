package com.lankatrails.lankatrails_backend.dtos.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Partial update of the signed-in user's preferences: null fields keep their current value. */
@Getter
@Setter
@NoArgsConstructor
public class UserPreferencesUpdateRequest {
    @Size(max = 10)
    private String preferredCurrency;
    @Size(max = 50)
    private String timeZone;
    @Size(max = 20)
    private String language;
    private Boolean is24Hour;
    @Pattern(regexp = "metric|imperial")
    private String measurementSystem;
    private Boolean notificationsEnabled;
    private Boolean emailNotifications;
    private Boolean pushNotifications;
    private Boolean smsNotifications;
}
