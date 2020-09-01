package com.huawei.hmspetstore.util;

import android.text.TextUtils;

public class Utils {

    public static String getRightString(String str) {
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        return str.split(",")[0];
    }

    public static Integer parseInt(String string) {
        Integer intValue = null;
        if (TextUtils.isEmpty(string)) {
            return intValue;
        }

        try {
            intValue = Integer.parseInt(string);
        } catch (NumberFormatException e) {

            intValue = null;
        }

        return intValue;
    }
}