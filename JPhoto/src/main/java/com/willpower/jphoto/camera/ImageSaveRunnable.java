package com.willpower.jphoto.camera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.Image;
import android.util.Log;

import com.willpower.jphoto.Utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * 图片保存线程
 */
class ImageSaveRunnable implements Runnable {
    private final Image mImage;
    private final File mFile;
    private final OnTakePictureCallback callback;
    private final boolean isFront;
    private final int rotation;

    public ImageSaveRunnable(OnTakePictureCallback callback, Image mImage, File mFile, boolean isFront,int rotation) {
        this.mImage = mImage;
        this.mFile = mFile;
        this.callback = callback;
        this.isFront = isFront;
        this.rotation = rotation;
    }


    @Override
    public void run() {
        ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        Matrix matrix = new Matrix();
        if (isFront) {// 镜像水平翻转
            matrix.postScale(-1, 1);
        }
        matrix.postRotate(rotation);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArray);
        FileOutputStream output = null;
        InputStream input = null;
        try {
            mFile.createNewFile();
            output = new FileOutputStream(mFile);
            input = new ByteArrayInputStream(byteArray.toByteArray());
            int x;
            byte[] b = new byte[1024 * 100];
            while ((x = input.read(b)) != -1) {
                output.write(b, 0, x);
            }
            Log.d(Utils.TAG, "save Image finish ");
            this.callback.onComplete(mFile);
        } catch (Exception e) {
            e.printStackTrace();
            this.callback.onComplete(null);
            Log.e(Utils.TAG, "save Image error: ", e);
        } finally {
            try {
                mImage.close();
                input.close();
                output.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
