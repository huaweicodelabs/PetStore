package com.huawei.hmspetstore.bean;

import java.util.List;

public class MainItemBean{

    // 列表类型
    private int viewType;
    // 作者
    private String author;
    // 距离
    private String distance;
    // 视频
    private VideoBean videoBean;
    // 商店图片列表
    private List<ImageBean> imageBeans;

    public MainItemBean(int viewType) {
        this.viewType = viewType;
    }

    public int getViewType() {
        return viewType;
    }

    public void setViewType(int viewType) {
        this.viewType = viewType;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public VideoBean getVideoBean() {
        return videoBean;
    }

    public void setVideoBean(VideoBean videoBean) {
        this.videoBean = videoBean;
    }

    public List<ImageBean> getImageBeans() {
        return imageBeans;
    }

    public void setImageBeans(List<ImageBean> imageBeans) {
        this.imageBeans = imageBeans;
    }
}