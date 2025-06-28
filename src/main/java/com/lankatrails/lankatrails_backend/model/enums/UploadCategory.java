package com.lankatrails.lankatrails_backend.model.enums;

import lombok.Getter;

@Getter
public enum UploadCategory {
    PROFILE_PICTURE("profile-pictures"),
    SERVICE_PICTURE("service-pictures"),
    DOCUMENT("documents");

    private final String directory;

    UploadCategory(String directory) {
        this.directory = directory;
    }

    public String getDirectory() {
        return directory;
    }
}
