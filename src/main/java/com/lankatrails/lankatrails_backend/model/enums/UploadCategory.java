package com.lankatrails.lankatrails_backend.model.enums;

import lombok.Getter;

@Getter
public enum UploadCategory {
    PROFILE_PICTURE("profile-pictures"),
    SERVICE_PICTURE("service-pictures"),
    DOCUMENT("documents"),
    COVER_PICTURE("cover-pictures"),
    BUSINESS_REGISTRATION("business-registration"),
    IDENTIFICATION("identification"),
    LICENCE("licence"),
    CHAT_IMAGE("chat-images"),
    CHAT_FILE("chat-files");


    private final String directory;

    UploadCategory(String directory) {
        this.directory = directory;
    }

}
