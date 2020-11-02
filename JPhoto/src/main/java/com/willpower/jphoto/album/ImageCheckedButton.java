package com.willpower.jphoto.album;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.willpower.jphoto.R;

public class ImageCheckedButton extends androidx.appcompat.widget.AppCompatImageView {
    public ImageCheckedButton(@NonNull Context context) {
        super(context);
        changeState(false);
    }

    public ImageCheckedButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        changeState(false);
    }

    public ImageCheckedButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        changeState(false);
    }

    public void changeState(boolean checked) {
        if (checked) {
            Drawable drawable = getResources().getDrawable(R.drawable.icon_checked);
            drawable.setTint(getResources().getColor(R.color.JPrimary));
            setImageDrawable(drawable);
        } else {
            setImageDrawable(getResources().getDrawable(R.drawable.icon_normal));
        }
    }
}
