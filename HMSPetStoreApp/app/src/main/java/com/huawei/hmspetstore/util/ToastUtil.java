/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.hmspetstore.util;

import android.content.Context;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

/**
 * 功能描述: 弹出Toast工具类.
 */
public class ToastUtil {
    private static final String TAG = "ToastUtil";

    private static final int MIN_TIME_INTERVAL = 500;

    private static ToastUtil sInstance;

    private static final Object LOCK = new Object();

    private Toast mLastToast;

    private long mLastTime;

    /**
     * init
     *
     * @return ToastUtil
     */
    public static ToastUtil getInstance() {
        synchronized (LOCK) {
            if (null == sInstance) {
                sInstance = new ToastUtil();
            }
        }
        return sInstance;
    }

    private ToastUtil() {
    }

    /**
     * showShort
     *
     * @param context context
     * @param resId   resId
     */
    public void showShort(Context context, int resId) {
        show(context, context.getString(resId), Toast.LENGTH_LONG);
    }

    /**
     * showShort
     *
     * @param context context
     * @param text    text
     */
    public void showShort(Context context, String text) {
        show(context, text, Toast.LENGTH_LONG);
    }

    /**
     * showLong
     *
     * @param context context
     * @param resId   resId
     */
    public void showLong(Context context, int resId) {
        show(context, context.getString(resId), Toast.LENGTH_LONG);
    }

    private void show(Context context, String msg, int durationType) {
        if (null == context || TextUtils.isEmpty(msg)) {
            Log.e(TAG, "show context is null or msg is empty");
            return;
        }

        if (SystemClock.elapsedRealtime() - mLastTime < MIN_TIME_INTERVAL) {
            return;
        }

        Toast toast = Toast.makeText(context, msg, durationType);
        if (null != mLastToast) {
            mLastToast.cancel();
        }
        mLastTime = SystemClock.elapsedRealtime();
        mLastToast = toast;
        toast.show();
    }

    /**
     * 取消Toast 显示
     */
    public void cancelToast() {
        mLastTime = 0L;
        if (null != mLastToast) {
            mLastToast.cancel();
        }
    }
}