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
public class APIResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private Object details;
}