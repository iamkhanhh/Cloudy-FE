package com.example.cloudstorage.models;

public class PresignedUrl {
    private String url;
    private String uploadName;
    private String contentType;

    public PresignedUrl(String url, String uploadName, String contentType) {
        this.url = url;
        this.uploadName = uploadName;
        this.contentType = contentType;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUploadName() {
        return uploadName;
    }

    public void setUploadName(String uploadName) {
        this.uploadName = uploadName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
