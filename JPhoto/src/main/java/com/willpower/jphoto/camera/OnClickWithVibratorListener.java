package com.willpower.jphoto.camera;

import android.view.View;

import com.willpower.jphoto.Utils;

public abstract class OnClickWithVibratorListener implements View.OnClickListener {

    private long count = 0;

    @Override
    public void onClick(View v) {
        if (System.currentTimeMillis() - count < 1000L) return;
        if (v != null) Utils.vibrator(v.getContext(), 50);
        onClickWithVibrator(v);
    }

    public abstract void onClickWithVibrator(View v);
}
