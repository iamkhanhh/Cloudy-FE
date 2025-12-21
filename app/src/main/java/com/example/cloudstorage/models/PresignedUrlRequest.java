package com.example.cloudstorage.models;

public class PresignedUrlRequest {
    private String fileName;
    private Integer albumId;

    public PresignedUrlRequest(String fileName, Integer albumId) {
        this.fileName = fileName;
        this.albumId = albumId;
    }

    public PresignedUrlRequest(String fileName) {
        this.fileName = fileName;
        this.albumId = null;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Integer getAlbumId() {
        return albumId;
    }

    public void setAlbumId(Integer albumId) {
        this.albumId = albumId;
    }
}
