package com.willpower.jphoto;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Point;
import android.hardware.camera2.CameraCharacteristics;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.willpower.jphoto.album.JImage;

import java.io.File;

public class Utils {
    public static final String TAG = "JPhoto";
    public static final SparseIntArray _BACK_ORIENTATIONS = new SparseIntArray();
    public static final SparseIntArray _FRONT_ORIENTATIONS = new SparseIntArray();

    static {
        _BACK_ORIENTATIONS.append(Surface.ROTATION_0, 90);
        _BACK_ORIENTATIONS.append(Surface.ROTATION_90, 0);
        _BACK_ORIENTATIONS.append(Surface.ROTATION_180, 270);
        _BACK_ORIENTATIONS.append(Surface.ROTATION_270, 180);

        _FRONT_ORIENTATIONS.append(Surface.ROTATION_0, 270);
        _FRONT_ORIENTATIONS.append(Surface.ROTATION_90, 0);
        _FRONT_ORIENTATIONS.append(Surface.ROTATION_180, 90);
        _FRONT_ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    /**
     * 从指定的屏幕旋转检索JPEG方向。
     */
    public static int getOrientation(int facing, int rotation, int orientation) {
        if (facing == CameraCharacteristics.LENS_FACING_BACK) {
            return (_BACK_ORIENTATIONS.get(rotation) + orientation + 270) % 360;
        } else {
            return (_FRONT_ORIENTATIONS.get(rotation) + orientation + 270) % 360;
        }
    }

    public static int[] getScreenSize(Activity activity, int sensorOrientation) {
        WindowManager windowManager = activity.getWindowManager();
        // 找出是否需要交换尺寸以获得相对于传感器坐标的预览尺寸。
        int displayRotation = windowManager.getDefaultDisplay().getRotation();
        boolean swappedDimensions = false;
        switch (displayRotation) {
            case Surface.ROTATION_0:
            case Surface.ROTATION_180:
                if (sensorOrientation == 90 || sensorOrientation == 270) {
                    swappedDimensions = true;
                }
                break;
            case Surface.ROTATION_90:
            case Surface.ROTATION_270:
                if (sensorOrientation == 0 || sensorOrientation == 180) {
                    swappedDimensions = true;
                }
                break;
        }

        Point displaySize = new Point();
        windowManager.getDefaultDisplay().getRealSize(displaySize);
        int[] maxPreviewSize = new int[2];
        if (swappedDimensions) {//横屏
            maxPreviewSize[0] = displaySize.x;
            maxPreviewSize[1] = displaySize.y;
        } else {//竖屏
            maxPreviewSize[0] = displaySize.y;
            maxPreviewSize[1] = displaySize.x;
        }
        return maxPreviewSize;
    }

    public static void setStatusBarTransparent(Activity activity) {
        Window window = activity.getWindow();
        View decorView = window.getDecorView();
        int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        decorView.setSystemUiVisibility(option);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
    }

    public static void vibrator(Context context, long duration) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(duration);
    }

    public static String getRootPath() {
        //首先判断外部存储是否可用
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return Environment.getExternalStorageDirectory().getPath();
        }
        return "/storage/emulated/0";
    }

    public static Uri getContentUri(Context context, int type, String path) {
        if (type == JImage.IMAGE) {
            return getImage(context, path);
        } else {
            return getVideo(context, path);
        }
    }

    private static Uri getImage(Context context, String path) {
        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID}, MediaStore.Images.Media.DATA + "=? ",
                new String[]{path}, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            // 如果图片不在手机的共享图片数据库，就先把它插入。
            if (new File(path).exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, path);
                return context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }

    private static Uri getVideo(Context context, String path) {
        Cursor cursor = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Video.Media._ID}, MediaStore.Video.Media.DATA + "=? ",
                new String[]{path}, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/video/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            // 如果图片不在手机的共享图片数据库，就先把它插入。
            if (new File(path).exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Video.Media.DATA, path);
                return context.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                Log.d(TAG, "getVideo: null");
                return null;
            }
        }
    }
}
