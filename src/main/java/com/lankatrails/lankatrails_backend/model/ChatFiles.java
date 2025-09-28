package com.lankatrails.lankatrails_backend.model;

import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "chat_files")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatFiles {
    @Id
    private String id; // MongoDB document ID

    private String fileName; // Name of the file

    private String fileType; // Type of the file (e.g., image/png, application/pdf)

    private String fileUrl; // URL where the file is stored

}
