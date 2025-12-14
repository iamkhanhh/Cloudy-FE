package com.example.cloudstorage.models;

import com.google.gson.annotations.SerializedName;

/**
 * Media model representing image or video from backend
 * Response from GET /api/v1/media/{id}
 */
public class Media {

    @SerializedName("id")
    private int id;

    @SerializedName("is_deleted")
    private int isDeleted;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("updatedAt")
    private String updatedAt;

    @SerializedName("owner_id")
    private int ownerId;

    @SerializedName("type")
    private String type; // "IMAGE" or "VIDEO"

    @SerializedName("mime_type")
    private String mimeType;

    @SerializedName("filename")
    private String filename;

    @SerializedName("size")
    private long size; // File size in bytes

    @SerializedName("duration_ms")
    private Integer durationMs; // null for images

    @SerializedName("visibility")
    private String visibility;

    @SerializedName("processing_status")
    private String processingStatus;

    @SerializedName("caption")
    private String caption;

    @SerializedName("file_path")
    private String filePath; // S3 URL

    // Constructors
    public Media() {}

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(int isDeleted) {
        this.isDeleted = isDeleted;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public Integer getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Integer durationMs) {
        this.durationMs = durationMs;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public String getProcessingStatus() {
        return processingStatus;
    }

    public void setProcessingStatus(String processingStatus) {
        this.processingStatus = processingStatus;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    // Utility methods
    public boolean isImage() {
        return "IMAGE".equalsIgnoreCase(type);
    }

    public boolean isVideo() {
        return "VIDEO".equalsIgnoreCase(type);
    }

    /**
     * Format file size to human-readable string
     * @return formatted size (e.g., "2.0 MB")
     */
    public String getFormattedSize() {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.1f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", size / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", size / (1024.0 * 1024.0 * 1024.0));
        }
    }

    /**
     * Format createdAt date to simple format
     * @return formatted date (e.g., "2025-11-20")
     */
    public String getFormattedCreatedAt() {
        if (createdAt != null && createdAt.length() >= 10) {
            return createdAt.substring(0, 10);
        }
        return createdAt;
    }
}
