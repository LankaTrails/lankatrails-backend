package com.lankatrails.lankatrails_backend.exception;

import org.springframework.http.HttpStatus;

/** 502: Stripe rejected or failed a call made on the user's behalf. */
public class PaymentGatewayException extends BaseException {

    public PaymentGatewayException(String technicalMessage, String userMessage) {
        super(HttpStatus.BAD_GATEWAY, ErrorCode.PAYMENT_GATEWAY_ERROR, technicalMessage, userMessage);
    }
}
