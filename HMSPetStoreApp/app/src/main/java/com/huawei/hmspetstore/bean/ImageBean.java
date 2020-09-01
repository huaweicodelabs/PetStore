package com.huawei.hmspetstore.bean;

import androidx.annotation.DrawableRes;

public class ImageBean{

    // 图片资源ID
    private int imageId;

    public ImageBean(@DrawableRes int imageId) {
        this.imageId = imageId;
    }

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }
}