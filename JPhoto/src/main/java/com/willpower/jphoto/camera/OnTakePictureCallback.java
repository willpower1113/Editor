package com.willpower.jphoto.camera;

import java.io.File;

/**
 * 拍照回调
 */
public interface OnTakePictureCallback {
    void onComplete(File file);
}
