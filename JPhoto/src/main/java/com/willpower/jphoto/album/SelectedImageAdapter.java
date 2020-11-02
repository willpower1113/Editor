package com.willpower.jphoto.album;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.willpower.jphoto.R;
import com.willpower.jphoto.Utils;

import java.util.ArrayList;
import java.util.List;

public class SelectedImageAdapter extends RecyclerView.Adapter<SelectedImageAdapter.ViewHolder> {
    private Context mContext;
    private List<JImage> mData;
    private View mTarget;
    private OnItemClickListener onItemClickListener;

    public SelectedImageAdapter(Context mContext,RecyclerView v, OnItemClickListener onItemClickListener) {
        this.mContext = mContext;
        this.mData = new ArrayList<>();
        this.mTarget = v;
        this.onItemClickListener = onItemClickListener;
    }

    public void addData(JImage image) {
        if (mTarget.getVisibility() == View.GONE){
            mTarget.setVisibility(View.VISIBLE);
        }
        Log.d(Utils.TAG, "selected image " + image.getDisplayName());
        this.mData.add(image);
        notifyDataSetChanged();
    }

    public void removeData(JImage image) {
        if (this.mData.contains(image)) {
            Log.d(Utils.TAG, "removed image " + image.getDisplayName());
            this.mData.remove(image);
            notifyDataSetChanged();
            if (mData.size() == 0) {
                mTarget.setVisibility(View.GONE);
            }
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_selected_image, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final JImage data = mData.get(position);
        Glide.with(mContext)
                .load(Utils.getContentUri(mContext,data.getType(), data.getPath()))
                .placeholder(R.drawable.icon_placeholder)
                .error(R.drawable.icon_placeholder)
                .centerCrop()
                .into(holder.imgSelected);
        holder.imgSelected.setOnClickListener(v -> onItemClickListener.onItemClick(data, v, position));
        holder.imgDelete.setOnClickListener(v -> onItemClickListener.onItemClick(data, v, position));
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        AutoFitImageView imgSelected;
        ImageView imgDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgSelected = itemView.findViewById(R.id.imgSelected);
            imgDelete = itemView.findViewById(R.id.imgDelete);
        }
    }
}
