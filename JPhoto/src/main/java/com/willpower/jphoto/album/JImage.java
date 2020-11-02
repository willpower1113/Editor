package com.willpower.jphoto.album;

public class JImage {
    public static final String DEFAULT_PARENT = "所有相册";
    public static final int IMAGE = 1;
    public static final int VIDEO = 2;
    public static final int ALL = 3;
    private String displayName;
    private int type;
    private boolean isGif;
    private String path;
    private String thumbnailPath;//视频缩略图
    private String parent;
    private boolean isChecked;

    public JImage() {
        this.isChecked = false;
        this.isGif = false;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public boolean isGif() {
        return isGif;
    }

    public void setGif(boolean gif) {
        isGif = gif;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
