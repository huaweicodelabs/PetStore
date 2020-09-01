/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.hmspetstore.util;

import android.net.Uri;

import com.huawei.hmspetstore.R;

/**
 * 功能描述: 播放工具
 */
public abstract class VideoUtil {

    /**
     * 获取 raw 中播放视频的 Uri
     */
    public static Uri getRawVideoUri(String videoName) {
        int videoId = 0;
        try {
            videoId = R.raw.class.getDeclaredField(videoName).getInt(new Object());
        } catch (Exception e) {

            return null;
        }
        return Uri.parse("android.resource://com.huawei.hmspetstore/" + videoId);
    }
}