package com.lankatrails.lankatrails_backend.dtos.request;

import com.lankatrails.lankatrails_backend.model.TabsSection;
import jakarta.validation.constraints.Size;
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
    @Size(max = TabsSection.CONTENT_MAX_LENGTH, message = "Tab content is too long")
    private String content;
}
