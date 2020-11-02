package com.willpower.jphoto.camera;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.hardware.camera2.CaptureRequest;
import android.os.Bundle;
import android.transition.Slide;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.willpower.jphoto.R;
import com.willpower.jphoto.Utils;

public class JCameraApi2Activity extends AppCompatActivity implements OnCameraStateChangedListener {
    private JCameraApi2DisplayView mCamera;
    private ImageView mFlashControl;
    private ImageView imgSwitch;
    private ImageView imgTakePicture;
    private ImageView imgClose;

    public static void start(Activity activity) {
        activity.startActivity(new Intent(activity, JCameraApi2Activity.class),
                ActivityOptions.makeSceneTransitionAnimation(activity)
                        .toBundle());
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.setStatusBarTransparent(this);
        getWindow().setEnterTransition(new Slide().setDuration(600));
        getWindow().setExitTransition(new Slide().setDuration(600));
        setContentView(R.layout.activity_camera);
        initView();
    }

    private OnClickWithVibratorListener clickListener = new OnClickWithVibratorListener() {
        @Override
        public void onClickWithVibrator(View v) {
            int id = v.getId();
            if (id == R.id.imgSwitch) {
                mCamera.switchCamera();
            } else if (id == R.id.imgTakePicture) {
                mCamera.takePicture(getExternalFilesDir(null) + "/" + System.currentTimeMillis() + ".jpg",
                        file -> Toast.makeText(JCameraApi2Activity.this, "保存成功：" + file.getPath(), Toast.LENGTH_LONG).show());
            } else if (id == R.id.imgClose) {
                onBackPressed();
            } else if (id == R.id.mFlashControl) {
                changeFlashMode();
            }
        }
    };

    private void initView() {
        imgSwitch = findViewById(R.id.imgSwitch);
        imgSwitch.setOnClickListener(clickListener);
        imgTakePicture = findViewById(R.id.imgTakePicture);
        imgTakePicture.setOnClickListener(clickListener);
        imgClose = findViewById(R.id.imgClose);
        imgClose.setOnClickListener(clickListener);
        mFlashControl = findViewById(R.id.mFlashControl);
        mFlashControl.setOnClickListener(clickListener);
        mCamera = findViewById(R.id.mTextureView);
        mCamera.onCreate(JCameraApi2DisplayView._BACK);
        mCamera.setCameraStateChangedListener(this);
        mFlashControl.setImageResource(R.drawable.icon_flash_close);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCamera.onResume();
    }

    @Override
    protected void onPause() {
        mCamera.onPause();
        super.onPause();
    }

    @Override
    public void onCameraOpened() {
        runOnUiThread(() -> {
            if (mCamera.supportFlash()) {
                mFlashControl.setVisibility(View.VISIBLE);
            } else {
                mFlashControl.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onCameraOpenFailed() {
        runOnUiThread(() -> finish());
    }


    /* CaptureRequest.FLASH_MODE_OFF 关闭
     * CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH 自动
     * CaptureRequest.FLASH_MODE_TORCH 常开 */
    public void changeFlashMode() {
        switch (mCamera.getFlashMode()) {
            case CaptureRequest.FLASH_MODE_OFF:
                mCamera.setFlashMode(CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                mFlashControl.setImageResource(R.drawable.icon_flash_auto);
                break;
            case CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH:
                mCamera.setFlashMode(CaptureRequest.FLASH_MODE_SINGLE);
                mFlashControl.setImageResource(R.drawable.icon_flash_open);
                break;
            default:
                mCamera.setFlashMode(CaptureRequest.FLASH_MODE_OFF);
                mFlashControl.setImageResource(R.drawable.icon_flash_close);
                break;
        }
    }
}
