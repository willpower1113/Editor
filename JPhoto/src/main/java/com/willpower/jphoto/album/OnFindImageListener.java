package com.willpower.jphoto.album;

import java.util.List;

public interface OnFindImageListener {
    void onComplete(List<JImage> imageList,List<String> albumList);
}
