package com.lankatrails.lankatrails_backend.dtos;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatFilesDto {
    private String id; // MongoDB document ID
    private String fileName; // Name of the file
    private String fileType; // Type of the file (e.g., image/png, application/pdf)
    private String fileUrl; // URL where the file is stored
}
