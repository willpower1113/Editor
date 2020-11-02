package com.willpower.jphoto.camera;

import android.app.Activity;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Build;
import android.util.Log;
import android.util.Size;

import androidx.annotation.RequiresApi;

import com.willpower.jphoto.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
class JDevice {
    private static Parameter frontCamera;//前置摄像头
    private static Parameter backCamera;//后置摄像头

    public static void init(CameraManager manager, Activity activity) {
        try {
            for (String id : manager.getCameraIdList()) {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(id);
                JDevice.addCamera(activity, id, characteristics, getSupportSize(characteristics));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(Utils.TAG, "Camera Info 初始化异常: ", e);
        }
    }

    public static Parameter getCameraInfo(int orientation) {
        switch (orientation) {
            case JCameraApi2DisplayView._FRONT:
                return frontCamera;
            default:
                return backCamera;
        }
    }


    static Size[] getSupportSize(CameraCharacteristics cameraCharacteristics) {
        StreamConfigurationMap streamConfigurationMap = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        Size[] mSizeArray = streamConfigurationMap != null ? streamConfigurationMap.getOutputSizes(ImageFormat.JPEG) : new Size[0];
        return mSizeArray;
    }

    static void addCamera(Activity activity, String cameraId, CameraCharacteristics characteristics, Size[] sizes) {
        Integer orientation = characteristics.get(CameraCharacteristics.LENS_FACING);
        if (orientation == null) orientation = -1;
        switch (orientation) {
            case CameraCharacteristics.LENS_FACING_FRONT:
                frontCamera = new Parameter(cameraId, characteristics, sizes, Parameter.mathPreviewSize(activity, sizes, orientation));
                break;
            case CameraCharacteristics.LENS_FACING_BACK:
                backCamera = new Parameter(cameraId, characteristics, sizes, Parameter.mathPreviewSize(activity, sizes, orientation));
                break;
        }
    }

    public static class Parameter {
        public String cameraId;//摄像头ID
        public CameraCharacteristics characteristics;//摄像头方向
        public Size[] supportSize;
        public Size previewSize;//期望预览尺寸
        public Size captureSize;//期望拍照尺寸

        public Parameter(String cameraId, CameraCharacteristics characteristics, Size[] sizes, Size previewSize) {
            this(cameraId, characteristics, sizes, previewSize, Collections.max(Arrays.asList(sizes), new CompareSizesByArea()));
        }

        public Parameter(String cameraId, CameraCharacteristics characteristics, Size[] supportSize, Size previewSize, Size captureSize) {
            this.cameraId = cameraId;
            this.characteristics = characteristics;
            this.supportSize = supportSize;
            this.previewSize = previewSize;
            this.captureSize = captureSize;
        }

        /*
        摄像头方向
         */
        public int getFacing() {
            return characteristics.get(CameraCharacteristics.LENS_FACING);
        }

        /*
        旋转角度
         */
        public int getOrientation() {
            return characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        }

        /*
        是否支持闪光灯
         */
        public boolean getAvailable() {
            return characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
        }

        public boolean supportAutoFocus() {
            int result = 0;
            for (int mode : characteristics.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES)) {
                Log.d(Utils.TAG, "getAFMode: " + mode);
                result += mode;
            }
            return result > 0;
        }

        static Size mathPreviewSize(Activity activity, Size[] supportSize, int sensorOrientation) {
            int[] maxPreviewSize = Utils.getScreenSize(activity, sensorOrientation);
            // 收集至少与预览图面一样大的支持分辨率
            List<Size> enoughArray = new ArrayList<>();
            Log.d(Utils.TAG, "屏幕尺寸: [width]=" + maxPreviewSize[0] + ",[height]=" + maxPreviewSize[1]);
            // 收集小于预览曲面的支持分辨率
            for (Size option : supportSize) {
                if (option.getHeight() == maxPreviewSize[1] && option.getWidth() <= maxPreviewSize[0]) {
                    Log.d(Utils.TAG, "option: [width]=" + option.getWidth() + ",[height]=" + option.getHeight());
                    enoughArray.add(option);
                }
            }
            //  先从大的里边选最小的，没有再从小的里边选最大的
            if (enoughArray.size() > 0) {
                Size choice = Collections.max(enoughArray, new CompareSizesByArea());
                Log.d(Utils.TAG, "choices: [width]=" + choice.getWidth() + ",[height]=" + choice.getHeight());
                return choice;
            } else {
                Log.e(Utils.TAG, "找不到任何合适的预览大小");
                return supportSize[0];
            }
        }
    }
}
