package com.willpower.jphoto.album;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class AutoFitImageView extends androidx.appcompat.widget.AppCompatImageView {
    public AutoFitImageView(@NonNull Context context) {
        super(context);
    }

    public AutoFitImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AutoFitImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = View.MeasureSpec.getSize(widthMeasureSpec);
        View.MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(width, width);
    }
}
