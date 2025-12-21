package com.example.cloudstorage.models;

public class CreateShareRequest {
    private String resource_type;  
    private int resource_id;       
    private String permission;     
    private String receiver_email; 


    public CreateShareRequest(String resourceType, int resourceId, String permission, String receiverEmail) {
        this.resource_type = resourceType;
        this.resource_id = resourceId;
        this.permission = permission;
        this.receiver_email = receiverEmail;
    }


    public String getResourceType() {
        return resource_type;
    }

    public void setResourceType(String resourceType) {
        this.resource_type = resourceType;
    }

    public int getResourceId() {
        return resource_id;
    }

    public void setResourceId(int resourceId) {
        this.resource_id = resourceId;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public String getReceiverEmail() {
        return receiver_email;
    }

    public void setReceiverEmail(String receiverEmail) {
        this.receiver_email = receiverEmail;
    }
}
