package com.willpower.jphoto.camera;

import android.content.Context;

public class CameraImpl implements JPhoto {

    ICamera iCamera;

    public CameraImpl(Context context) {
        iCamera = isSupportCamera2() ? new CameraApi2() : new CameraApi1(context);
    }

    @Override
    public boolean isSupportCamera2() {
        return false;
    }

    @Override
    public void photograph(String path) {

    }

    @Override
    public void recordVideo(String path) {

    }

    @Override
    public void recordComplete() {

    }

    @Override
    public void cut() {

    }

    @Override
    public void compress() {

    }
}
