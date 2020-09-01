/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.hmspetstore.util;

import android.content.Context;

import com.huawei.hmspetstore.R;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * 功能描述: 系统工具类.
 */
public abstract class SystemUtil {
    private static final String TAG = "SystemUtil";

    /**
     * dp to px
     *
     * @param context context
     * @param dp      dp
     * @return int
     */
    public static int dp2px(Context context, float dp) {
        if (null == context) {
            return 0;
        }
        if (dp <= 0.1f) {
            return 0;
        }
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public static String getApiKey(Context context) {
        if (context == null) {
            return "";
        }
        String apiKey = context.getString(R.string.api_key);
        try {
            apiKey = URLEncoder.encode(apiKey, "UTF-8");
        } catch (UnsupportedEncodingException ignored) {

        }
        return apiKey;
    }
}