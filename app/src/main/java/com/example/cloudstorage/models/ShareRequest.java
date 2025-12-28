// File: app/src/main/java/com/example/cloudstorage/models/ShareRequest.java
package com.example.cloudstorage.models;

import com.google.gson.annotations.SerializedName;

public class ShareRequest {

    @SerializedName("to_email")
    private String toEmail;

    @SerializedName("subject")
    private String subject;

    @SerializedName("body")
    private String body;

    @SerializedName("media_id")
    private String mediaId;

    public ShareRequest(String toEmail, String subject, String body, String mediaId) {
        this.toEmail = toEmail;
        this.subject = subject;
        this.body = body;
        this.mediaId = mediaId;
    }

    public String getToEmail() {
        return toEmail;
    }
    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    public String getMediaId() {
        return mediaId;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setToEmail(String toEmail) {
        this.toEmail = toEmail;
    }
}
    