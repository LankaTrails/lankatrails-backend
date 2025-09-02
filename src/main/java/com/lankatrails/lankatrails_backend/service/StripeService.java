package com.lankatrails.lankatrails_backend.service;

import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;

public interface StripeService {
    APIResponse<String> handleStripeWebhook(String payload, String sigHeader);
}
