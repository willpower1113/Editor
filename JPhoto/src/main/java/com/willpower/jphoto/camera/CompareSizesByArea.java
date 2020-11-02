package com.willpower.jphoto.camera;

import android.os.Build;
import android.util.Size;

import androidx.annotation.RequiresApi;

import java.util.Comparator;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class CompareSizesByArea implements Comparator<Size> {

    @Override
    public int compare(Size lhs, Size rhs) {
        //我们在这里进行转换以确保乘法不会溢出
        return lhs.getHeight() * lhs.getWidth() - rhs.getHeight() * rhs.getWidth();
    }
}
