package com.willpower.jphoto.camera;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;

public interface ICamera {
    String TAG = "ICamera";
    int CAMERA_FACING_BACK = 0;
    int CAMERA_FACING_FRONT = 1;

    Camera getCamera();

    void openCamera(boolean isBack);

    void startPreview(SurfaceTexture surface,Camera.PreviewCallback callback);

    void closeCamera();

    void photograph(boolean openShutter,byte[] data);

    void record();

    boolean hasCamera();
}
