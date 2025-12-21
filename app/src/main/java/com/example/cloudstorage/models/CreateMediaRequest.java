package com.example.cloudstorage.models;

public class CreateMediaRequest {
    private String type;          
    private String mime_type;     
    private String filename;      
    private long size;            
    private Integer duration_ms;  
    private String visibility;    
    private String processing_status;
    private String caption;       
    private String file_path;     
    private Integer albumsId;     

    // Default constructor
    public CreateMediaRequest() {
    }

    // Builder pattern for easy construction
    public static class Builder {
        private CreateMediaRequest request = new CreateMediaRequest();

        public Builder type(String type) {
            request.type = type;
            return this;
        }

        public Builder mimeType(String mimeType) {
            request.mime_type = mimeType;
            return this;
        }

        public Builder filename(String filename) {
            request.filename = filename;
            return this;
        }

        public Builder size(long size) {
            request.size = size;
            return this;
        }

        public Builder durationMs(Integer durationMs) {
            request.duration_ms = durationMs;
            return this;
        }

        public Builder visibility(String visibility) {
            request.visibility = visibility;
            return this;
        }

        public Builder processingStatus(String processingStatus) {
            request.processing_status = processingStatus;
            return this;
        }

        public Builder caption(String caption) {
            request.caption = caption;
            return this;
        }

        public Builder filePath(String filePath) {
            request.file_path = filePath;
            return this;
        }

        public Builder albumsId(Integer albumsId) {
            request.albumsId = albumsId;
            return this;
        }

        public CreateMediaRequest build() {
            return request;
        }
    }

    // Getters and setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMimeType() {
        return mime_type;
    }

    public void setMimeType(String mimeType) {
        this.mime_type = mimeType;
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
        return duration_ms;
    }

    public void setDurationMs(Integer durationMs) {
        this.duration_ms = durationMs;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public String getProcessingStatus() {
        return processing_status;
    }

    public void setProcessingStatus(String processingStatus) {
        this.processing_status = processingStatus;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getFilePath() {
        return file_path;
    }

    public void setFilePath(String filePath) {
        this.file_path = filePath;
    }

    public Integer getAlbumsId() {
        return albumsId;
    }

    public void setAlbumsId(Integer albumsId) {
        this.albumsId = albumsId;
    }
}
