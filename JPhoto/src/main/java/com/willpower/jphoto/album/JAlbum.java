package com.willpower.jphoto.album;

import java.util.List;

public class JAlbum {
    private String path;
    private String name;
    private List<JImage> images;

    public JAlbum(String name,String path) {
        this.name = name;
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<JImage> getImages() {
        return images;
    }

    public void setImages(List<JImage> images) {
        this.images = images;
    }

    public void addImage(JImage image) {
        this.images.add(image);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
