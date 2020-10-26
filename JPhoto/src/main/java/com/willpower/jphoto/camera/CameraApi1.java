package com.willpower.jphoto.camera;


import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CameraApi1 implements ICamera {
    private Camera mCamera;
    private Context mContext;

    public CameraApi1(Context context) {
        mContext = context;
    }


    @Override
    public Camera getCamera() {
        return mCamera;
    }

    @Override
    public void openCamera(boolean isBack) {
        mCamera = createCameraSafely(isBack);
        mCamera.setDisplayOrientation(90);
    }

    @Override
    public void startPreview(SurfaceTexture surface, Camera.PreviewCallback callback) {
        try {
            mCamera.setPreviewTexture(surface);
            mCamera.setPreviewCallback(callback);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void closeCamera() {
        mCamera.stopPreview();
        mCamera.setPreviewCallback(null);
    }

    @Override
    public void photograph(boolean openShutter,byte[] data) {
        PhotoUtils.saveBitmapToFile(data,PhotoUtils.PHOTO_DIR);
    }

    @Override
    public void record() {
    }

    @Override
    public boolean hasCamera() {
        if (mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 创建相机实例
     */
    private Camera createCameraSafely(boolean isBack) {
        if (!hasCamera()) return null;
        if (isBack) {
            return Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
        } else {
            return Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
        }
    }
}
