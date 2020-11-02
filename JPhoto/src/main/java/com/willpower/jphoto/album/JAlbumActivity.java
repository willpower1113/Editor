package com.willpower.jphoto.album;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.transition.Slide;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.willpower.jphoto.R;
import com.willpower.jphoto.Utils;

import java.util.ArrayList;
import java.util.List;

public class JAlbumActivity extends AppCompatActivity implements OnFindImageListener {
    private Spinner spinnerFolder;
    private ImageView imgClose;
    private RecyclerView recyclerAlbum, recyclerChoose;
    private Button btnConfirm;
    private TextView tvNotes;
    private TabLayout mTabGroup;

    private DisplayImageAdapter displayImageAdapter;
    private SelectedImageAdapter selectedImageAdapter;

    public static void start(Activity activity) {
        activity.startActivity(new Intent(activity, JAlbumActivity.class),
                ActivityOptions.makeSceneTransitionAnimation(activity)
                        .toBundle());
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setEnterTransition(new Slide().setDuration(600));
        getWindow().setExitTransition(new Slide().setDuration(600));
        setContentView(R.layout.activity_album);
        initView();
        new Thread(new SearchImagesRunnable(this, this, JImage.ALL)).start();
    }

    private void initView() {
        spinnerFolder = findViewById(R.id.spinnerFolder);
        imgClose = findViewById(R.id.imgClose);
        recyclerAlbum = findViewById(R.id.recyclerAlbum);
        recyclerChoose = findViewById(R.id.recyclerChoose);
        btnConfirm = findViewById(R.id.btnConfirm);
        tvNotes = findViewById(R.id.tvNotes);
        mTabGroup = findViewById(R.id.mTabGroup);
        initFolder();
        initTabGroup();
        initDisplay();
        initSelected();
    }

    private void initFolder() {
        List<String> mData = new ArrayList<>();
        mData.add("相册1");
        mData.add("相册1");
        mData.add("相册1");
        mData.add("相册1");
        mData.add("相册1");
        // 建立Adapter并且绑定数据源
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.j_spinner, mData);
        adapter.setDropDownViewResource(R.layout.j_spinner_item);
        //绑定 Adapter到控件
        spinnerFolder.setAdapter(adapter);
    }

    private void initTabGroup() {
        mTabGroup.addTab(mTabGroup.newTab().setText("全部"));
        mTabGroup.addTab(mTabGroup.newTab().setText("图片"));
        mTabGroup.addTab(mTabGroup.newTab().setText("视频"));
        mTabGroup.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (imageList == null) return;
                switch (tab.getPosition()) {
                    case 0:
                        displayImageAdapter.setNewData(getList(JImage.ALL));
                        break;
                    case 1:
                        displayImageAdapter.setNewData(getList(JImage.IMAGE));
                        break;
                    case 2:
                        displayImageAdapter.setNewData(getList(JImage.VIDEO));
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void initDisplay() {
        recyclerAlbum.setLayoutManager(new GridLayoutManager(this, 4));
        displayImageAdapter = new DisplayImageAdapter(this, (data, v, position) -> {
            int viewId = v.getId();
            if (viewId == R.id.mChecked) {
                changeSelected(data.isChecked(), data);
            }
        });
        recyclerAlbum.setAdapter(displayImageAdapter);
    }

    Handler mHandler = new Handler(Looper.getMainLooper());

    @SuppressLint("DefaultLocale")
    private void changeSelected(boolean isChecked, JImage data) {
        if (isChecked) {
            selectedImageAdapter.addData(data);
        } else {
            selectedImageAdapter.removeData(data);
        }
        btnConfirm.setText(String.format("下一步（%d）", selectedImageAdapter.getItemCount()));
        mHandler.postDelayed(() -> recyclerChoose.scrollToPosition(selectedImageAdapter.getItemCount() - 1), 100);
    }

    private void initSelected() {
        recyclerChoose.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        selectedImageAdapter = new SelectedImageAdapter(this, recyclerChoose, (data, v, position) -> {
            int viewId = v.getId();
            if (viewId == R.id.imgDelete) {
                changeSelected(false, data);
            }
        });
        recyclerChoose.setAdapter(selectedImageAdapter);
    }

    List<JImage> imageList;
    List<String> albumList;

    @Override
    public void onComplete(List<JImage> imageList, List<String> albumList) {
        Log.e(Utils.TAG, "搜索完毕，图片【" + imageList.size() + "】，相册【" + albumList.size() + "】");
        this.imageList = imageList;
        this.albumList = albumList;
        runOnUiThread(() -> displayImageAdapter.setNewData(imageList));
    }

    private List<JImage> getList(int type) {
        if (type == JImage.ALL) return imageList;
        List<JImage> temp = new ArrayList<>();
        for (JImage image : imageList) {
            if (image.getType() == type) {
                temp.add(image);
            }
        }
        return temp;
    }
}
