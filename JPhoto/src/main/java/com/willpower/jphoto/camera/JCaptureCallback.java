package com.willpower.jphoto.camera;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.os.Build;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.willpower.jphoto.Utils;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
abstract class JCaptureCallback extends CameraCaptureSession.CaptureCallback {
    //预览
    public static final int STATE_PREVIEW = 0;
    //等待焦点锁定
    public static final int STATE_WAITING_LOCK = 1;
    //没有自动对焦的镜头
    public static final int STATE_NOT_SUPPORT_AUTO_FOCUS = 2;
    //等待曝光处于曝光状态
    public static final int STATE_WAITING_PREVIEW = 3;
    //等待曝光状态不是预曝光
    public static final int STATE_WAITING_NON_PREVIEW = 4;
    //拍照完成
    public static final int STATE_PICTURE_TAKEN = 5;
    //等待焦点锁定
    public static final int STATE_UN_LOCK = 6;
    //记录当前状态
    private int mState;

    public int getState() {
        return mState;
    }

    public void changeState(int mState) {
        this.mState = mState;
    }

    @Override
    public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {
        Log.e(Utils.TAG, "Capture failed: " + failure.getReason());
    }

    @Override
    public void onCaptureBufferLost(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull Surface target, long frameNumber) {
        Log.e(Utils.TAG, "Capture Buffer Lost: ");
    }

    @Override
    public void onCaptureSequenceAborted(@NonNull CameraCaptureSession session, int sequenceId) {
        super.onCaptureSequenceAborted(session, sequenceId);
        Log.e(Utils.TAG, "Capture Sequence Aborted: ");
    }

    @Override
    public void onCaptureSequenceCompleted(@NonNull CameraCaptureSession session, int sequenceId, long frameNumber) {
        super.onCaptureSequenceCompleted(session, sequenceId, frameNumber);
        Log.e(Utils.TAG, "Capture Sequence Completed: ");
    }

    @Override
    public void onCaptureProgressed(@NonNull CameraCaptureSession session,
                                    @NonNull CaptureRequest request,
                                    @NonNull CaptureResult partialResult) {
        process(partialResult);
    }

    @Override
    public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                   @NonNull CaptureRequest request,
                                   @NonNull TotalCaptureResult result) {
        process(result);
    }

    //捕获
    protected abstract void onCaptureStillPicture() throws CameraAccessException;

    //预捕获
    protected abstract void onPreCaptureSequence() throws CameraAccessException;

    //锁定焦点
    protected abstract void onUnLockFocus() throws CameraAccessException;

    /**
     * 真实处理函数
     *
     * @param result
     */
    private void process(CaptureResult result) {
        switch (mState) {
            case STATE_PREVIEW: { // 当相机预览正常工作时，我们什么也不做。
                break;
            }
            case STATE_WAITING_LOCK: {
                Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
                Log.e(Utils.TAG, "process: " + afState);
                if (afState == null) {
                    try {
                        onCaptureStillPicture();
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                        Log.d(Utils.TAG, "捕获静态图片异常: ", e);
                    }
                } else if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState ||
                        CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState) {
                    // CONTROL_AE_STATE 在某些设备上可以为空
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeState == null || aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                        mState = STATE_PICTURE_TAKEN;
                        try {
                            onCaptureStillPicture();
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                            Log.d(Utils.TAG, "捕获静态图片异常: ", e);
                        }
                    } else {
                        try {
                            onPreCaptureSequence();
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                            Log.d(Utils.TAG, "预捕获异常: ", e);
                        }
                    }
                }
                break;
            }
            case STATE_NOT_SUPPORT_AUTO_FOCUS:{
                try {
                    mState = STATE_PICTURE_TAKEN;
                    onPreCaptureSequence();
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                    Log.d(Utils.TAG, "预捕获异常: ", e);
                }
                break;
            }
            case STATE_WAITING_PREVIEW: {
                // CONTROL_AE_STATE 在某些设备上可以为空
                Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                if (aeState == null ||
                        aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||
                        aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED) {
                    mState = STATE_WAITING_NON_PREVIEW;
                }
                break;
            }
            case STATE_WAITING_NON_PREVIEW: {
                // CONTROL_AE_STATE 在某些设备上可以为空
                Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                    mState = STATE_PICTURE_TAKEN;
                    try {
                        onCaptureStillPicture();
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                        Log.d(Utils.TAG, "捕获静态图片异常: ", e);
                    }
                }
                break;
            }
            case STATE_UN_LOCK: {
                try {
                    onUnLockFocus();
                    Log.d(Utils.TAG, "onUnLockFocus: ");
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                    Log.d(Utils.TAG, "锁定焦点异常: ", e);
                }
                break;
            }
        }
    }
}
