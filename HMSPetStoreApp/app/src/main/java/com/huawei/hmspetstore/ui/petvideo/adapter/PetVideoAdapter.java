package com.huawei.hmspetstore.ui.petvideo.adapter;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.huawei.hmspetstore.R;
import com.huawei.hmspetstore.bean.VideoBean;
import com.huawei.hmspetstore.common.IRecyclerItemListener;
import com.huawei.hmspetstore.ui.center.MemberCenterAct;
import com.huawei.hmspetstore.ui.center.MemberRight;
import com.huawei.hmspetstore.util.VideoUtil;

import java.util.List;

public class PetVideoAdapter extends RecyclerView.Adapter<PetVideoAdapter.ItemViewHolder> {

    private List<VideoBean> mItems;
    private IRecyclerItemListener listener;
    private VideoView localVideoView;
    private ImageView localImageView;
    private ImageView localStartPlay;
    private ImageView localFullScreen;
    private MediaController mediaController;

    private Uri videoUri;

    public PetVideoAdapter(List<VideoBean> mItems) {
        this.mItems = mItems;
        // "android.resource://com.huawei.hmspetstore/" + R.raw.video;
        videoUri = VideoUtil.getRawVideoUri("video");
    }

    /**
     * listener
     */
    public void setListener(IRecyclerItemListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.petvideo_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ItemViewHolder holder, final int position) {
        holder.textView.setText(mItems.get(position).getVideoName());
        holder.imageView.setVisibility(View.VISIBLE);
        Glide.with(holder.itemView.getContext())
                .load(videoUri)
                .thumbnail(0.25f)
                .error(R.mipmap.video_img)
                .into(holder.imageView);

        holder.startPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPlayVideo(holder);
            }
        });

        holder.fullScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemClick(holder.itemView, position);
                    holder.startPlay.setVisibility(View.VISIBLE);
                    holder.imageView.setVisibility(View.VISIBLE);
                    holder.videoView.setVisibility(View.GONE);
                    holder.fullScreen.setVisibility(View.GONE);
                    onDestroy();
                }
            }
        });
    }

    /**
     * 播放视频
     */
    private void onPlayVideo(@NonNull final ItemViewHolder holder) {
        if (videoUri == null) {
            return;
        }
        // Check 是否可以播放视频
        if (!canPlayVideo(holder.itemView.getContext())) {
            return;
        }
        onDestroy();
        holder.videoView.setVisibility(View.VISIBLE);
        holder.fullScreen.setVisibility(View.VISIBLE);
        holder.startPlay.setVisibility(View.GONE);
        holder.imageView.setVisibility(View.GONE);
        this.localVideoView = holder.videoView;
        this.localImageView = holder.imageView;
        this.localStartPlay = holder.startPlay;
        this.localFullScreen = holder.fullScreen;
        //创建MediaController对象
        mediaController = new MediaController(holder.itemView.getContext());
        mediaController.setVisibility(View.VISIBLE);
        //加载指定的视频文件 本地或者网络视频地址
        holder.videoView.setVideoURI(videoUri);
        //VideoView与MediaController建立关联
        holder.videoView.setMediaController(mediaController);
        holder.videoView.requestFocus();
        // 播放监听
        onPlayListener(holder);
    }

    /**
     * 播放监听
     */
    private void onPlayListener(@NonNull final ItemViewHolder holder) {
        holder.videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                //让VideoView获取焦点
                holder.videoView.start();
                if (mediaController != null) {
                    mediaController.show();
                }
            }
        });

        holder.videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                holder.startPlay.setVisibility(View.VISIBLE);
                holder.imageView.setVisibility(View.VISIBLE);
                holder.videoView.setVisibility(View.GONE);
                holder.fullScreen.setVisibility(View.GONE);
                onDestroy();
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.videoView.isPlaying()) {
                    if (mediaController != null && !mediaController.isShowing()) {
                        mediaController.show();
                    }
                } else {
                    if (holder.imageView.getVisibility() != View.VISIBLE) {
                        if (mediaController != null && !mediaController.isShowing()) {
                            mediaController.show();
                        }
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mItems == null ? 0 : mItems.size();
    }

    /**
     * Check 是否可以播放视频
     */
    private boolean canPlayVideo(Context context) {
        if (MemberRight.isVideoAvailable(context)) {
            return true;
        }
        context.startActivity(new Intent(context, MemberCenterAct.class));
        return false;
    }

    /**
     * 清理正在播放的视频
     */
    public void onDestroy() {
        if (mediaController != null) {
            mediaController.hide();
            mediaController = null;
        }
        if (localVideoView != null) {
            localVideoView.stopPlayback();
            localVideoView.setVisibility(View.GONE);
            localVideoView = null;
        }

        if (localFullScreen != null) {
            localFullScreen.setVisibility(View.GONE);
            localFullScreen = null;
        }

        if (localImageView != null) {
            localImageView.setVisibility(View.VISIBLE);
            localImageView = null;
        }

        if (localStartPlay != null) {
            localStartPlay.setVisibility(View.VISIBLE);
            localStartPlay = null;
        }
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        VideoView videoView;
        ImageView fullScreen;
        ImageView imageView;
        ImageView startPlay;

        ItemViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.petvideo_item_videoName);
            videoView = itemView.findViewById(R.id.petvideo_item_videoView);
            fullScreen = itemView.findViewById(R.id.petvideo_item_fullScreen);
            imageView = itemView.findViewById(R.id.petvideo_item_imageView);
            startPlay = itemView.findViewById(R.id.petvideo_item_startPlay);
        }
    }
}