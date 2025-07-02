package com.lankatrails.lankatrails_backend.dtos.response;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProfilePicResponse {
    private String profilePicUrl;
    private Long userId;
}
