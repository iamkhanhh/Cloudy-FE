package com.example.cloudstorage.models;

import com.google.gson.annotations.SerializedName;

/**
 * Album model representing a folder/album from backend
 * Response from GET /api/v1/albums
 */
public class Album {

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

    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName("cover_media_id")
    private Integer coverMediaId; // null if no cover

    @SerializedName("visibility")
    private String visibility;

    // Constructors
    public Album() {}

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getCoverMediaId() {
        return coverMediaId;
    }

    public void setCoverMediaId(Integer coverMediaId) {
        this.coverMediaId = coverMediaId;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    // Utility methods
    /**
     * Format createdAt date to simple format
     * @return formatted date (e.g., "December 20, 2025")
     */
    public String getFormattedCreatedAt() {
        if (createdAt != null && createdAt.length() >= 10) {
            // Extract year, month, day from ISO format
            String[] parts = createdAt.substring(0, 10).split("-");
            if (parts.length == 3) {
                String year = parts[0];
                String month = getMonthName(parts[1]);
                String day = parts[2];
                return month + " " + day + ", " + year;
            }
        }
        return createdAt;
    }

    private String getMonthName(String monthNumber) {
        switch (monthNumber) {
            case "01": return "January";
            case "02": return "February";
            case "03": return "March";
            case "04": return "April";
            case "05": return "May";
            case "06": return "June";
            case "07": return "July";
            case "08": return "August";
            case "09": return "September";
            case "10": return "October";
            case "11": return "November";
            case "12": return "December";
            default: return monthNumber;
        }
    }
}
