package com.lankatrails.lankatrails_backend.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class APIResponse<T> {
    private boolean success;
    private String message;
    private T data;
}