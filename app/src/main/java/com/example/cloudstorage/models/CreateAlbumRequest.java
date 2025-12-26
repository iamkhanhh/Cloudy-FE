package com.example.cloudstorage.models;

public class CreateAlbumRequest {
    private String name;
    private String visibility;
    private String description;
    public CreateAlbumRequest() {
    }

    // Builder pattern for easy construction
    public static class Builder {
        private final CreateAlbumRequest request = new CreateAlbumRequest();

        public CreateAlbumRequest.Builder name(String name) {
            request.name = name;
            return this;
        }

        public CreateAlbumRequest.Builder visibility(String visibility) {
            request.visibility = visibility;
            return this;
        }

        public CreateAlbumRequest.Builder description(String description) {
            request.description = description;
            return this;
        }

        public CreateAlbumRequest build() {
            return request;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
