package com.example.cloudstorage.models;

import com.google.gson.annotations.SerializedName;

/**
 * Share model representing a shared resource (Media or Album) from backend
 * Response from GET /api/v1/shares
 */
public class Share {

    @SerializedName("id")
    private int id;

    @SerializedName("is_deleted")
    private int isDeleted;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("updatedAt")
    private String updatedAt;

    @SerializedName("resource_type")
    private String resourceType; // "MEDIA" or "ALBUM"

    @SerializedName("resource_id")
    private int resourceId;

    @SerializedName("owner_id")
    private int ownerId;

    @SerializedName("permission")
    private String permission; // "VIEW", "EDIT", etc.

    @SerializedName("receiver_id")
    private int receiverId;

    @SerializedName("content")
    private ShareContent content;

    // Nested class for polymorphic content
    public static class ShareContent {
        // Fields common to both Media and Album
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

        // Media-specific fields
        @SerializedName("type")
        private String type; // "IMAGE" or "VIDEO" (null for albums)

        @SerializedName("mime_type")
        private String mimeType;

        @SerializedName("filename")
        private String filename;

        @SerializedName("size")
        private Long size;

        @SerializedName("duration_ms")
        private Integer durationMs;

        @SerializedName("visibility")
        private String visibility;

        @SerializedName("processing_status")
        private String processingStatus;

        @SerializedName("caption")
        private String caption;

        @SerializedName("file_path")
        private String filePath;

        // Album-specific fields
        @SerializedName("name")
        private String name;

        @SerializedName("description")
        private String description;

        @SerializedName("cover_media_id")
        private Integer coverMediaId;

        // Getters
        public int getId() { return id; }
        public String getCreatedAt() { return createdAt; }
        public String getType() { return type; }
        public String getFilename() { return filename; }
        public String getName() { return name; }
        public String getFilePath() { return filePath; }
        public Long getSize() { return size; }
        public String getCaption() { return caption; }

        public boolean isImage() {
            return "IMAGE".equalsIgnoreCase(type);
        }

        public boolean isVideo() {
            return "VIDEO".equalsIgnoreCase(type);
        }

        public String getFormattedSize() {
            if (size == null) return "N/A";
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

        public String getFormattedCreatedAt() {
            if (createdAt != null && createdAt.length() >= 10) {
                return createdAt.substring(0, 10);
            }
            return createdAt;
        }
    }

    // Constructors
    public Share() {}

    // Getters
    public int getId() { return id; }
    public String getCreatedAt() { return createdAt; }
    public String getResourceType() { return resourceType; }
    public int getResourceId() { return resourceId; }
    public String getPermission() { return permission; }
    public ShareContent getContent() { return content; }

    // Utility methods
    public boolean isMedia() {
        return "MEDIA".equalsIgnoreCase(resourceType);
    }

    public boolean isAlbum() {
        return "ALBUM".equalsIgnoreCase(resourceType);
    }

    /**
     * Get display name (filename for media, name for album)
     */
    public String getDisplayName() {
        if (content == null) return "Unknown";
        if (isMedia()) {
            return content.getFilename() != null ? content.getFilename() : "Untitled Media";
        } else if (isAlbum()) {
            return content.getName() != null ? content.getName() : "Untitled Album";
        }
        return "Unknown";
    }

    /**
     * Get formatted date to display
     */
    public String getDisplayDate() {
        if (content != null && content.getCreatedAt() != null) {
            return content.getFormattedCreatedAt();
        }
        return "";
    }

    /**
     * Convert to FolderItem for display compatibility
     */
    public FolderItem toFolderItem() {
        if (isMedia()) {
            // Create a Media object from ShareContent
            Media media = new Media();
            media.setId(content.getId());
            media.setFilename(content.getFilename());
            media.setType(content.getType());
            media.setFilePath(content.getFilePath());
            media.setSize(content.getSize() != null ? content.getSize() : 0);
            media.setCaption(content.getCaption());
            media.setCreatedAt(content.getCreatedAt());
            return new FolderItem(media);
        } else if (isAlbum()) {
            // Create an Album object from ShareContent
            Album album = new Album();
            album.setId(content.getId());
            album.setName(content.getName());
            album.setDescription(content.getDescription());
            album.setCreatedAt(content.getCreatedAt());
            album.setCoverMediaId(content.getCoverMediaId());
            return new FolderItem(album);
        }
        return null;
    }
}
