/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.hmspetstore.util;


import android.util.Log;

/**
 * 功能描述: String工具类.
 */
public abstract class StringUtil {
    private static final String TAG = "StringUtils";

    /**
     * toString
     *
     * @param value value
     * @return String
     */
    public static String toString(Object value) {
        if (value instanceof String) {
            return (String) value;
        } else if (value != null) {
            return String.valueOf(value);
        } else {
            Log.i(TAG, "toString invalid");
        }
        return "";
    }
}