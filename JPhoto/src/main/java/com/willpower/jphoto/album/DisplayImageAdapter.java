package com.willpower.jphoto.album;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.willpower.jphoto.R;
import com.willpower.jphoto.Utils;

import java.util.List;


public class DisplayImageAdapter extends RecyclerView.Adapter<DisplayImageAdapter.ViewHolder> {
    private Context mContext;
    private List<JImage> mData;
    private OnItemClickListener onItemClickListener;

    public DisplayImageAdapter(Context mContext, OnItemClickListener onItemClickListener) {
        this.mContext = mContext;
        this.onItemClickListener = onItemClickListener;
    }

    public void setNewData(List<JImage> data) {
        this.mData = data;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_display_image, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final JImage data = mData.get(position);
        if (data.isGif()) {
            holder.gifTag.setVisibility(View.VISIBLE);
        } else {
            holder.gifTag.setVisibility(View.GONE);
        }
        if (data.getType() == JImage.VIDEO) {
            holder.videoTag.setVisibility(View.VISIBLE);
        } else {
            holder.videoTag.setVisibility(View.GONE);
        }
        Glide.with(mContext)
                .load(Utils.getContentUri(mContext, data.getType(), data.getPath()))
                .centerCrop()
                .placeholder(R.drawable.icon_placeholder)
                .error(R.drawable.icon_placeholder)
                .override(holder.imgDisplay.getWidth(), holder.imgDisplay.getHeight())
                .into(holder.imgDisplay);
        holder.imgChecked.changeState(data.isChecked());
        holder.imgDisplay.setOnClickListener(v -> onItemClickListener.onItemClick(data, v, position));
        holder.imgChecked.setOnClickListener(v -> {
            data.setChecked(!data.isChecked());
            notifyItemChanged(position);
            onItemClickListener.onItemClick(data, v, position);
        });
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        AutoFitImageView imgDisplay;
        ImageCheckedButton imgChecked;
        ImageView gifTag;
        ImageView videoTag;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgDisplay = itemView.findViewById(R.id.imgDisplay);
            imgChecked = itemView.findViewById(R.id.mChecked);
            gifTag = itemView.findViewById(R.id.gifTag);
            videoTag = itemView.findViewById(R.id.videoTag);
        }
    }
}
