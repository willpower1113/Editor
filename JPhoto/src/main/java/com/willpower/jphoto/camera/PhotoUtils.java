package com.willpower.jphoto.camera;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PhotoUtils {
    private static final String TAG = "JPhoto";
    public static final File PHOTO_DIR = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
    private static final SimpleDateFormat picFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    public static void saveBitmapToFile(byte[] data,File photoDir) {
        FileOutputStream fos = null;
        try {
            if (!photoDir.exists()) photoDir.mkdirs();
            File save = new File(PHOTO_DIR, picFormat.format(new Date()) + ".png");
            fos = new FileOutputStream(save);
            Log.e(TAG, "save photo: " + save.getAbsolutePath());
        } catch (Exception e) {
            Log.e(TAG, "save photo error: ", e);
        } finally {
            try {
                fos.write(data);
                fos.flush();
                fos.close();
            } catch (Exception e) {
                Log.e(TAG, "save photo error: ", e);
            }
        }
    }

}
