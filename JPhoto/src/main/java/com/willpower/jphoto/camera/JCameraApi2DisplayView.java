package com.willpower.jphoto.camera;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.willpower.jphoto.Utils;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;


import static com.willpower.jphoto.camera.JCaptureCallback.STATE_NOT_SUPPORT_AUTO_FOCUS;
import static com.willpower.jphoto.camera.JCaptureCallback.STATE_WAITING_LOCK;

/**
 * 相机预览控件
 */
class JCameraApi2DisplayView extends TextureView implements TextureView.SurfaceTextureListener {
    public static final int _BACK = CameraCharacteristics.LENS_FACING_BACK;
    public static final int _FRONT = CameraCharacteristics.LENS_FACING_FRONT;
    //预览 session
    private CameraCaptureSession mCaptureSession;
    //摄像头对象
    private CameraDevice mCameraDevice;
    //用来执行后台任务的线程
    private HandlerThread mBackgroundThread;
    //用于执行后台任务的Handler
    private Handler mBackgroundHandler;
    //拍照的ImageReader
    private ImageReader mImageReader;
    private File mFile;
    private OnTakePictureCallback mTakePictureCallback;
    //预览的 CaptureRequest.Builder
    private CaptureRequest.Builder mPreviewRequestBuilder;
    //预览的 CaptureRequest
    private CaptureRequest mPreviewRequest;
    //防止应用程序在关闭摄像头之前退出
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);
    private JDevice.Parameter currentDevice;
    private CameraManager mCameraManager;
    private int mFlashMode = CaptureRequest.FLASH_MODE_OFF;//闪光灯类型 默认关闭
    private OnCameraStateChangedListener stateChangedListener;
    private int mRatioWidth = 0;
    private int mRatioHeight = 0;

    public JCameraApi2DisplayView(@NonNull Context context) {
        super(context);
    }

    public JCameraApi2DisplayView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public JCameraApi2DisplayView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 设置宽高比例
     */
    private void setAspectRatio(int width, int height) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("尺寸不能为负数。");
        }
        mRatioWidth = width;
        mRatioHeight = height;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = View.MeasureSpec.getSize(widthMeasureSpec);
        int height = View.MeasureSpec.getSize(heightMeasureSpec);
        if (0 == mRatioWidth || 0 == mRatioHeight) {
            setMeasuredDimension(width, height);
        } else {
            if (width < height * mRatioWidth / mRatioHeight) {
                setMeasuredDimension(width, width * mRatioHeight / mRatioWidth);
            } else {
                setMeasuredDimension(height * mRatioWidth / mRatioHeight, height);
            }
        }
    }

    @Override
    public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
        openCamera();
        configureTransform(width, height, currentDevice.previewSize);
    }

    @Override
    public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {
        configureTransform(width, height, currentDevice.previewSize);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {

    }

    /**
     * 修改预览旋转角度，尺寸
     */
    private void configureTransform(int width, int height, Size previewSize) {
        WindowManager windowManager = ((Activity) getContext()).getWindowManager();
        int rotation = windowManager.getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, width, height);
        RectF bufferRect = new RectF(0, 0, previewSize.getHeight(), previewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max((float) height / previewSize.getHeight(),
                    (float) width / previewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        setTransform(matrix);
    }

    /*
    CameraDevice状态改变回调 */
    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            // 此方法在相机打开时调用。我们在这里开始摄像机预览。
            Log.d(Utils.TAG, "on Camera Opened: ");
            stateChangedListener.onCameraOpened();//对外部提供回调
            mCameraOpenCloseLock.release();
            mCameraDevice = cameraDevice;
            startPreview(getSurfaceTexture());
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            Log.d(Utils.TAG, "on Camera Disconnected: ");
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            Log.d(Utils.TAG, "on Camera onError: ");
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }
    };

    /*保存图片回调*/
    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Log.d(Utils.TAG, "拍照保存: " + mFile.getAbsolutePath());
            int rotation = ((Activity) getContext()).getWindowManager().getDefaultDisplay().getRotation();
            int orientation = Utils.getOrientation(currentDevice.getFacing(), rotation, currentDevice.getOrientation());
            mBackgroundHandler.post(new ImageSaveRunnable(mTakePictureCallback,
                    reader.acquireNextImage(), mFile,
                    currentDevice.getFacing() == _FRONT, orientation));
        }
    };

    /*处理图片捕获回调*/
    private JCaptureCallback mCaptureCallback = new JCaptureCallback() {
        @Override
        protected void onCaptureStillPicture() throws CameraAccessException {
            if (mCameraDevice == null) return;
            // 这是CaptureRequest.Builder我们用来拍照的
            mCaptureSession.stopRepeating();
            mCaptureSession.abortCaptures();
            mCaptureCallback.changeState(JCaptureCallback.STATE_UN_LOCK);
            mCaptureSession.capture(createCaptureRequest(), mCaptureCallback, null/*handler 传 null，代表在当前线程执行*/);
        }

        @Override
        protected void onPreCaptureSequence() throws CameraAccessException {
            // 这是如何告诉摄像机触发
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
                    CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START);
            // 告诉#mCaptureCallback等待设置预捕获序列。
            mCaptureCallback.changeState(STATE_WAITING_PREVIEW);
            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback,
                    mBackgroundHandler);
        }

        @Override
        protected void onUnLockFocus() throws CameraAccessException {
            // 重置自动对焦触发器
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                    CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
            configFlash(mPreviewRequestBuilder);
            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback,
                    mBackgroundHandler);
            // 在此之后，相机将回到正常的预览状态
            mCaptureCallback.changeState(STATE_PREVIEW);
            mCaptureSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback, mBackgroundHandler);
        }
    };

    /* 创建拍照请求 */
    private CaptureRequest createCaptureRequest() throws CameraAccessException {
        final CaptureRequest.Builder captureBuilder =
                mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
        captureBuilder.addTarget(mImageReader.getSurface());
        // 使用与预览相同的自动曝光和自动对焦模式
        captureBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
        configFlash(captureBuilder);
        return captureBuilder.build();
    }

    /*开启相机*/
    @SuppressLint("MissingPermission")
    private void openCamera() {
        try {
            //拍照 ImageReader
            mImageReader = ImageReader.newInstance(currentDevice.captureSize.getWidth(), currentDevice.captureSize.getHeight(), ImageFormat.JPEG, 2);
            mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mBackgroundHandler);
            //我们将TextureView的纵横比与我们选择的预览大小相匹配。
            int orientation = getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                setAspectRatio(currentDevice.previewSize.getWidth(), currentDevice.previewSize.getHeight());
            } else {
                setAspectRatio(currentDevice.previewSize.getHeight(), currentDevice.previewSize.getWidth());
            }
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS))
                throw new RuntimeException("摄像头开启超时.");
            mCameraManager.openCamera(currentDevice.cameraId, mStateCallback, mBackgroundHandler);
        } catch (CameraAccessException | InterruptedException | RuntimeException e) {
            e.printStackTrace();
            Log.e(Utils.TAG, "open Camera error：", e);
            stateChangedListener.onCameraOpenFailed();//对外部提供回调
        }
    }

    /*重新打开相机*/
    private void reOpenCamera() {
        if (isAvailable()) {
            openCamera();
        } else {
            setSurfaceTextureListener(this);
        }
    }

    /*开启预览*/
    private void startPreview(SurfaceTexture texture) {
        try {
            // 我们将默认缓冲区的大小配置为我们想要的摄影机预览的大小。
            texture.setDefaultBufferSize(currentDevice.previewSize.getWidth(), currentDevice.previewSize.getHeight());
            // 这是我们需要开始预览的输出曲面
            Surface surface = new Surface(texture);
            // 我们建立了一个CaptureRequest.Builder与输出曲面
            mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(surface);
            // 在这里，我们为相机预览创建一个CameraCaptureSession
            mCameraDevice.createCaptureSession(Arrays.asList(surface, mImageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            // 摄像机已经关闭
                            if (mCameraDevice == null) return;
                            // 当会话准备好后，我们开始显示预览。
                            mCaptureSession = cameraCaptureSession;
                            try {
                                // 设置自动对焦
                                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                                // 必要时自动启用闪光灯.
                                configFlash(mPreviewRequestBuilder);
                                mPreviewRequest = mPreviewRequestBuilder.build();
                                mCaptureSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback, mBackgroundHandler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                                Log.e(Utils.TAG, "预览失败！ ", e);
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                            Log.e(Utils.TAG, "预览失败！ ");
                        }
                    }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            Log.e(Utils.TAG, "预览失败！ ", e);
        }
    }

    /*关闭相机*/
    private void closeCamera() {
        try {
            mCameraOpenCloseLock.acquire();
            if (null != mCaptureSession) {
                mCaptureSession.close();
                mCaptureSession = null;
            }
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            if (null != mImageReader) {
                mImageReader.close();
                mImageReader = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("关闭摄像头中断.", e);
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    /*开启工作线程*/
    private void startBackgroundThread() {
        if (mBackgroundThread != null) return;
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    /*关闭工作线程*/
    private void stopBackgroundThread() {
        if (mBackgroundThread == null) return;
        try {
            mBackgroundThread.quitSafely();
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /*配置闪光灯*/
    private void configFlash(CaptureRequest.Builder builder) {
        if (mFlashMode == CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH)
            builder.set(CaptureRequest.CONTROL_AE_MODE, mFlashMode);
        else
            builder.set(CaptureRequest.FLASH_MODE, mFlashMode);
    }

    /**********************************************对外提供方法****************************************************/

    /**
     * 切换前置/后置 摄像头
     */
    public void switchCamera() {
        closeCamera();
        if (currentDevice != null && currentDevice.getFacing() == _BACK) {
            this.currentDevice = JDevice.getCameraInfo(_FRONT);
        } else {
            this.currentDevice = JDevice.getCameraInfo(_BACK);
        }
        reOpenCamera();
    }

    /**
     * 拍照
     */
    public void takePicture(String path, OnTakePictureCallback callback) {
        try {
            mFile = new File(path);
            if (!mFile.getParentFile().exists()) mFile.getParentFile().mkdirs();
            mTakePictureCallback = callback;
            if (currentDevice.supportAutoFocus()) {
                // 这是告诉相机如何锁定焦点.
                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);
                // 告诉#mCaptureCallback等待锁定
                mCaptureCallback.changeState(STATE_WAITING_LOCK);
            } else {
                mCaptureCallback.changeState(STATE_NOT_SUPPORT_AUTO_FOCUS);
            }
            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            Log.e(Utils.TAG, "takePicture: ", e);
        }
    }

    /**
     * 生命周期函数
     */
    public void onCreate(int facing) {
        Log.d(Utils.TAG, "JCameraApi2DisplayView onCreate");
        this.mCameraManager = (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);
        JDevice.init(mCameraManager, (Activity) getContext());
        this.currentDevice = JDevice.getCameraInfo(facing);
    }

    public void onResume() {
        startBackgroundThread();
        reOpenCamera();
    }

    public void onPause() {
        closeCamera();
        stopBackgroundThread();
    }

    /**
     * 是否支持闪光灯
     *
     * @return
     */
    public boolean supportFlash() {
        if (currentDevice == null) return false;
        Boolean support = currentDevice.getAvailable();
        return support;
    }

    /**
     * 设置闪光灯
     * CaptureRequest.FLASH_MODE_OFF 关闭
     * CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH 自动
     * CaptureRequest.FLASH_MODE_TORCH 常开
     */
    public void setFlashMode(int flashMode) {
        this.mFlashMode = flashMode;
    }

    public int getFlashMode() {
        return mFlashMode;
    }

    /**
     * 设置Camera状态回调
     */
    public void setCameraStateChangedListener(OnCameraStateChangedListener listener) {
        this.stateChangedListener = listener;
    }
}
