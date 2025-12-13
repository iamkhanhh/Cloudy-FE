package com.example.cloudstorage.models;

import com.google.gson.annotations.SerializedName;

/**
 * Model for storage data response from backend
 * Backend returns storage in GB for each media type
 */
public class StorageData {
    @SerializedName("IMAGE")
    private double image;

    @SerializedName("VIDEO")
    private double video;

    @SerializedName("TOTAL")
    private double total;

    public StorageData() {}

    public StorageData(double image, double video, double total) {
        this.image = image;
        this.video = video;
        this.total = total;
    }

    public double getImage() {
        return image;
    }

    public void setImage(double image) {
        this.image = image;
    }

    public double getVideo() {
        return video;
    }

    public void setVideo(double video) {
        this.video = video;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }
}
