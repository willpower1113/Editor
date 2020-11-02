package com.willpower.photo;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.willpower.jphoto.album.JAlbumActivity;
import com.willpower.jphoto.camera.JCameraApi2Activity;

public class DisplayActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);
    }

    public void onClickCamera(View v) {
        JCameraApi2Activity.start(this);
    }

    public void onClickAlbum(View v) {
        JAlbumActivity.start(this);
    }
}
