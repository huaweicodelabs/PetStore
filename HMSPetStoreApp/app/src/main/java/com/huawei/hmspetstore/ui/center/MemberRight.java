package com.huawei.hmspetstore.ui.center;

import android.content.Context;
import android.text.TextUtils;

import com.huawei.hms.iap.entity.InAppPurchaseData;
import com.huawei.hmspetstore.util.LoginUtil;
import com.huawei.hmspetstore.util.SPUtil;

/**
 * 文 件 名: MemberRight.java
 * 版    权: Copyright Huawei Tech.Co.Ltd. All Rights Reserved.
 * 描    述: 会员权限管理
 */
public class MemberRight {
    public static final String NORMAL_MEMBER = "normal";
    public static final String SUBSCRIPTION_MEMBER = "subscription";
    public static final String FOREVER_MEMBER = "forever";
    public static final long ONE_DAY = 24 * 60 * 60 * 1000;
    private static final String VIDEO_NORMAL_KEY = "videoNormalKey";
    private static final String VIDEO_FOREVER_KEY = "videoForeverKey";
    private static final String VIDEO_SUBSCRIPTION_KEY = "videoSubscriptionKey";
    private static final String UNKNOWN = "unknown";

    public static String getCurrentUserId(Context context) {
        String localUserUuid = LoginUtil.getLocalUserUuid(context);
        if (!TextUtils.isEmpty(localUserUuid)) {
            return localUserUuid;
        } else {
            return UNKNOWN;
        }
    }

    /**
     * 获取会员类型
     *
     * @param context context
     * @return string
     */
    public static String getMemberType(Context context) {
        if (isVideoAvailableForever(context)) {
            return FOREVER_MEMBER;
        }
        if (isVideoSubscriptionValid(context)) {
            return SUBSCRIPTION_MEMBER;
        }
        if (System.currentTimeMillis() < getNormalVideoExpireDate(context)) {
            return NORMAL_MEMBER;
        }
        return "";
    }

    /**
     * 是否有权限观看视频
     *
     * @param context 上下文
     * @return boolean
     */
    public static boolean isVideoAvailable(Context context) {
        return isVideoAvailableForever(context) || isVideoSubscriptionValid(context)
                || System.currentTimeMillis() < getNormalVideoExpireDate(context);
    }

    /**
     * 判断是否是永久会员
     *
     * @param context 上下文
     * @return boolean
     */
    public static boolean isVideoAvailableForever(Context context) {
        return (boolean) SPUtil.get(context, getCurrentUserId(context), VIDEO_FOREVER_KEY, false);
    }

    /**
     * 更新永久会员的状态
     *
     * @param context 上下文
     */
    public static void setVideoAvailableForever(Context context) {
        SPUtil.put(context, getCurrentUserId(context), VIDEO_FOREVER_KEY, true);
    }

    /**
     * 判断是否是订阅会员
     *
     * @param context 上下文
     * @return boolean
     */
    public static boolean isVideoSubscriptionValid(Context context) {
        return System.currentTimeMillis() < getVideoSubscriptionExpireDate(context);
    }

    /**
     * 更新订阅会员到期时间
     *
     * @param inAppPurchaseData 购买信息
     */
    public static void updateVideoSubscriptionExpireDate(Context context, InAppPurchaseData inAppPurchaseData) {
        if (inAppPurchaseData == null) {
            return;
        }
        // 订阅有效
        if (inAppPurchaseData.isSubValid()) {
            long expireDate = inAppPurchaseData.getExpirationDate();
            String uuid = inAppPurchaseData.getDeveloperPayload();
            if (TextUtils.isEmpty(uuid)) {
                uuid = getCurrentUserId(context);
            }
            long videoExpireDate = getVideoSubscriptionExpireDate(context);
            if (videoExpireDate < expireDate) {
                SPUtil.put(context, uuid, VIDEO_SUBSCRIPTION_KEY, expireDate);
            }
        }
    }

    /**
     * 获取订阅会员的有效期截止时间
     *
     * @param context 上下文
     * @return 时间戳
     */
    public static long getVideoSubscriptionExpireDate(Context context) {
        return (long) SPUtil.get(context, getCurrentUserId(context), VIDEO_SUBSCRIPTION_KEY, 0L);
    }

    /**
     * 普通会员有效期时间
     *
     * @param context 上下文
     * @return 时间戳
     */
    public static long getNormalVideoExpireDate(Context context) {
        return (long) SPUtil.get(context, getCurrentUserId(context), VIDEO_NORMAL_KEY, 0L);
    }

    /**
     * 更新普通会员有效期
     *
     * @param context   上下文
     * @param extension 有效时间段
     */
    public static void updateNormalVideoValidDate(Context context, long extension) {
        long videoExpireDate = getNormalVideoExpireDate(context);
        long currentTime = System.currentTimeMillis();
        if (currentTime < videoExpireDate) {
            videoExpireDate += extension;
        } else {
            videoExpireDate = currentTime + extension;
        }
        SPUtil.put(context, getCurrentUserId(context), VIDEO_NORMAL_KEY, videoExpireDate);
    }
}