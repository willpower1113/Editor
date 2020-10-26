package com.willpower.jphoto.camera;

import android.graphics.Camera;

public interface JCamera {

    boolean isSupportCamera2();

    Camera getCamera();

    void photograph(String path);
}
