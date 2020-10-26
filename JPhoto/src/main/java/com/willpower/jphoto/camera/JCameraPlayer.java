package com.willpower.jphoto.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Build;
import android.util.AttributeSet;
import android.view.TextureView;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

public class JCameraPlayer extends TextureView implements JPhoto, View.OnClickListener, TextureView.SurfaceTextureListener, Camera.PreviewCallback {

    ICamera iCamera;

    public JCameraPlayer(@NonNull Context context) {
        super(context);
        init();
    }

    public JCameraPlayer(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public JCameraPlayer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public JCameraPlayer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setSurfaceTextureListener(this);
        iCamera = isSupportCamera2() ? new CameraApi2() : new CameraApi1(getContext());
    }

    @Override
    public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
        iCamera.openCamera(true);
        iCamera.startPreview(surface, this);
    }

    @Override
    public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {

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

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        frame = data;
    }

    private byte[] frame;

    @Override
    public void onClick(View v) {
        iCamera.photograph(false,frame);
    }
}
