package com.lankatrails.lankatrails_backend.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@Builder
@AllArgsConstructor
@Builder
public class APIResponse<T> {
    private HttpStatus status;
    private String message;
    private T data;

    public APIResponse(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}