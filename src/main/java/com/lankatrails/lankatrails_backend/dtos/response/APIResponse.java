package com.lankatrails.lankatrails_backend.dtos.response;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class APIResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private Object details = null;

    public APIResponse(boolean b, String s, T data) {
        this.success = b;
        this.message = s;
        this.data = data;
    }
}