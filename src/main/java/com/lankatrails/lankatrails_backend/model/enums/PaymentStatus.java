package com.lankatrails.lankatrails_backend.model.enums;

import lombok.Getter;

@Getter
public enum PaymentStatus {
    PENDING,
    COMPLETED,
    FAILED,
    REFUNDED
}
