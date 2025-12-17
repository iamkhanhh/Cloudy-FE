package com.example.cloudstorage.models;

/**
 * Wrapper class to combine Album and Media into a single list item
 * Used for displaying both albums and media in the same RecyclerView/FlexboxLayout
 */
public class FolderItem {
    public static final int TYPE_ALBUM = 0;
    public static final int TYPE_MEDIA = 1;

    private int type;
    private Album album;
    private Media media;

    // Constructor for Album
    public FolderItem(Album album) {
        this.type = TYPE_ALBUM;
        this.album = album;
        this.media = null;
    }

    // Constructor for Media
    public FolderItem(Media media) {
        this.type = TYPE_MEDIA;
        this.album = null;
        this.media = media;
    }

    public int getType() {
        return type;
    }

    public boolean isAlbum() {
        return type == TYPE_ALBUM;
    }

    public boolean isMedia() {
        return type == TYPE_MEDIA;
    }

    public Album getAlbum() {
        return album;
    }

    public Media getMedia() {
        return media;
    }

    // Utility methods to get common properties
    public String getName() {
        if (isAlbum()) {
            return album.getName();
        } else if (isMedia()) {
            return media.getFilename();
        }
        return "";
    }

    public String getDate() {
        if (isAlbum()) {
            return album.getFormattedCreatedAt();
        } else if (isMedia()) {
            return media.getFormattedCreatedAt();
        }
        return "";
    }

    public int getId() {
        if (isAlbum()) {
            return album.getId();
        } else if (isMedia()) {
            return media.getId();
        }
        return -1;
    }
}
