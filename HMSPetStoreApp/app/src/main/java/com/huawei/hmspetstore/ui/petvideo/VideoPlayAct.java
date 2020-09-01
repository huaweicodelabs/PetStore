package com.huawei.hmspetstore.ui.petvideo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.huawei.hmspetstore.R;
import com.huawei.hmspetstore.ui.center.MemberCenterAct;
import com.huawei.hmspetstore.ui.center.MemberRight;
import com.huawei.hmspetstore.util.ToastUtil;

/**
 * 宠物视频-播放
 */
public class VideoPlayAct extends AppCompatActivity {

    // 个人中心
    private static final int REQ_CODE_MEMBER_CENTER = 100;
    // 播放控件
    private VideoView mVideoView;
    // 控制控件
    private MediaController mediaController;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.videoplay_act);
        if (MemberRight.isVideoAvailable(this)) {
            play();
        } else {
            startActivityForResult(new Intent(this, MemberCenterAct.class), REQ_CODE_MEMBER_CENTER);
        }
    }

    /**
     * 播放视频
     */
    private void play() {
        // 初始化View
        initView();
        // 初始化播放
        initVideoPlay();
    }

    /**
     * 初始化View
     */
    private void initView() {
        ImageView imageView = findViewById(R.id.videoplay_delete);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mVideoView = findViewById(R.id.videoplay_videoView);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initVideoPlay() {
        //创建MediaController对象
        mediaController = new MediaController(this);
        //加载指定的视频文件 本地或者网络视频地址
        // 视频播放路径，本地或者网络视频地址 "android.resource://" + getPackageName() + "/" + R.raw.video
        String path = ("android.resource://" + getPackageName() + "/" + R.raw.video);
        mVideoView.setVideoURI(Uri.parse(path));
        //VideoView与MediaController建立关联
        mVideoView.setMediaController(mediaController);
        mVideoView.requestFocus();

        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                //让VideoView获取焦点
                mediaController.show();
                mVideoView.start();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE_MEMBER_CENTER) {
            if (MemberRight.isVideoAvailable(this)) {
                play();
            } else {
                finish();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mVideoView != null) {
            mVideoView.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mVideoView != null) {
            mVideoView.stopPlayback();
            mVideoView = null;
        }
    }
}