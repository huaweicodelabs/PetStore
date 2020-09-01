/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.hmspetstore.util;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.huawei.hmspetstore.bean.UserBean;
import com.huawei.hmspetstore.constant.SPConstants;
import com.huawei.hmspetstore.ui.login.LoginAct;

import java.util.List;

/**
 * 功能描述: 登录工具
 */
public abstract class LoginUtil {
    private static final String TAG = "LoginUtil";

    /**
     * 登录检测
     */
    public static boolean loginCheck(Context context) {
        boolean isLogin = isLogin(context);
        if (!isLogin) {
            context.startActivity(new Intent(context, LoginAct.class));
        }
        return isLogin;
    }

    /**
     * 判断是否登录
     */
    public static boolean isLogin(Context context) {
        if (null == context) {
            return false;
        }
        return (boolean) SPUtil.get(context, SPConstants.KEY_LOGIN, Boolean.FALSE);
    }

    /**
     * 判断是否登录
     */
    public static boolean isHuaweiLogin(Context context) {
        if (null == context) {
            return false;
        }
        return (boolean) SPUtil.get(context, SPConstants.KEY_HW_LOGIN, Boolean.FALSE);
    }


    /**
     * 获取当前账号登录的UUID
     * 华为账号：返回 openId
     * 非华为账号：根据用户名称获取 保存本地的uuid
     */
    public static String getLocalUserUuid(Context context) {
        if (null == context) {
            return "";
        }

        boolean isLogin = (boolean) SPUtil.get(context, SPConstants.KEY_LOGIN, false);
        if (!isLogin) {
            // 未登录
            return "";
        }

        boolean isHuaweiLogin = (boolean) SPUtil.get(context, SPConstants.KEY_HW_LOGIN, false);
        if (isHuaweiLogin) {
            // 华为账号登录，返回 openId
            return (String) SPUtil.get(context, SPConstants.KEY_HW_OEPNID, "");
        }

        // 非华为账号登录，根据当前登录用户名，获取 UUID
        String localStr = (String) SPUtil.get(context, SPConstants.KEY_LOCAL_USER, "");
        if (TextUtils.isEmpty(localStr)) {
            // 没有本地用户信息
            return "";
        }
        try {
            Gson gson = new Gson();
            List<UserBean> mLocalData = gson.fromJson(localStr, new TypeToken<List<UserBean>>() {
            }.getType());

            if (mLocalData == null || mLocalData.size() == 0) {
                // 没有本地用户
                return "";
            }
            String localName = (String) SPUtil.get(context, SPConstants.KEY_NICK_NAME, "");
            if (TextUtils.isEmpty(localName)) {
                // 未知是谁登录
                return "";
            }
            String uuid = "";
            for (UserBean bean : mLocalData) {
                if (localName.equals(bean.getName())) {
                    uuid = bean.getUuid();
                    break;
                }
            }
            return uuid;
        } catch (JsonSyntaxException ignored) {
            Log.e(TAG, "getLocalUserUuid JsonSyntaxException");
        }
        return "";
    }
}