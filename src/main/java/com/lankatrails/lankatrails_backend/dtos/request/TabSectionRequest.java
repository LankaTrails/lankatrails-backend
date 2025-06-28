package com.lankatrails.lankatrails_backend.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TabSectionRequest {
    private Long id;
    private String heading;
    private String content;
}
