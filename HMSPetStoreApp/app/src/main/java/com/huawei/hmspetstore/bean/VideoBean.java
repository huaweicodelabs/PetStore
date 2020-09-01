package com.huawei.hmspetstore.bean;

import java.io.Serializable;

public class VideoBean implements Serializable {

    private static final long serialVersionUID = -8312714910011484450L;

    // 视频名称
    private String videoName;
    // 视频播放地址
    private String videoUrl;

    public VideoBean(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public VideoBean(String videoName, String videoUrl) {
        this.videoName = videoName;
        this.videoUrl = videoUrl;
    }


    public String getVideoName() {
        return videoName;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }
}