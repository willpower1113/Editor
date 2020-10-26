package com.willpower.jphoto.camera;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;

public class CameraApi2 implements ICamera{

    @Override
    public Camera getCamera() {
        return null;
    }

    @Override
    public void openCamera(boolean isBack) {

    }

    @Override
    public void startPreview(SurfaceTexture surface, Camera.PreviewCallback callback) {

    }

    @Override
    public void closeCamera() {

    }

    @Override
    public void photograph(boolean openShutter, byte[] data) {

    }

    @Override
    public void record() {

    }

    @Override
    public boolean hasCamera() {
        return false;
    }
}
