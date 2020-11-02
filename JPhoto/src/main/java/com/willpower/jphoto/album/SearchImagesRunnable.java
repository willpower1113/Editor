package com.willpower.jphoto.album;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;

import com.willpower.jphoto.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SearchImagesRunnable implements Runnable {
    private final static String[] imageProjection = {MediaStore.Images.Media.DATA, MediaStore.Images.Media.SIZE, MediaStore.Images.Media.DISPLAY_NAME};
    private final static String[] videoProjection = {MediaStore.Video.Media.DATA, MediaStore.Images.Media.SIZE, MediaStore.Video.Media.DISPLAY_NAME, MediaStore.Video.Thumbnails.DATA};
    private OnFindImageListener listener;
    private Context context;
    private List<String> albumList;
    private List<JImage> imageList;
    private int mType;

    public SearchImagesRunnable(OnFindImageListener listener, Context context, int type) {
        this.listener = listener;
        this.context = context;
        this.mType = type;
        albumList = new ArrayList<>();//所有文件夹
        imageList = new ArrayList<>();
    }

    @Override
    public void run() {
        albumList.add(JImage.DEFAULT_PARENT);
        if (mType == JImage.IMAGE) {
            findImages();
        } else if (mType == JImage.VIDEO) {
            findVideos();
        } else {
            findImages();
            findVideos();
        }
        listener.onComplete(imageList, albumList);
    }

    private void findImages() {
        Cursor mCursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageProjection, null, null, MediaStore.Images.Media.DATE_MODIFIED + " desc");
        if (mCursor != null) {
            while (mCursor.moveToNext()) {
                String path = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.DATA));
                String displayName = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));
                if (mCursor.getLong(mCursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)) > 1024) {
                    JImage jImage = new JImage();
                    jImage.setType(JImage.IMAGE);
                    jImage.setPath(path);
                    jImage.setDisplayName(displayName);
                    if (displayName.endsWith("gif")) {
                        jImage.setGif(true);
                    }
                    imageList.add(jImage);
                    String dirPath = new File(path).getParentFile().getAbsolutePath();
                    if (!albumList.contains(dirPath)) {
                        albumList.add(dirPath);
                    }
                    Log.d(Utils.TAG, "文件名: " + path);
                }
            }
            mCursor.close();
        }
    }


    private void findVideos() {
        Cursor mCursor = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, videoProjection, null, null, MediaStore.Images.Media.DATE_MODIFIED + " desc");
        if (mCursor != null) {
            while (mCursor.moveToNext()) {
                String path = mCursor.getString(mCursor.getColumnIndex(MediaStore.Video.Media.DATA));
                String displayName = mCursor.getString(mCursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME));
                if (mCursor.getLong(mCursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)) > 1024) {
                    JImage jImage = new JImage();
                    jImage.setType(JImage.VIDEO);
                    jImage.setPath(path);
                    jImage.setDisplayName(displayName);
                    imageList.add(jImage);
                    String dirPath = new File(path).getParentFile().getAbsolutePath();
                    if (!albumList.contains(dirPath)) {
                        albumList.add(dirPath);
                    }
                    Log.d(Utils.TAG, "缩略图: " + path);
                }
            }
            mCursor.close();
        }
    }

}
