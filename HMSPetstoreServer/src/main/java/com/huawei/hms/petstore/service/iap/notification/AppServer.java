/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package com.huawei.hms.petstore.service.iap.notification;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

public class AppServer {
    // 验证公钥
    private static final String PUBLIC_KEY = "Your Public Key Of IAP Information";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * 处理通知信息
     *
     * @param information 请求内容
     */
    public static String dealNotification(String information) throws Exception {
        if (StringUtils.isEmpty(information)) {
            return "";
        }
        // 从IAP服务器回调的信息里解析出请求对象
        StatusUpdateNotificationRequest request = MAPPER.readValue(information, StatusUpdateNotificationRequest.class);
        StatusUpdateNotificationResponse response = new StatusUpdateNotificationResponse();

        if (StringUtils.isEmpty(request.getNotifycationSignature())
            || StringUtils.isEmpty(request.getStatusUpdateNotification())) {
            response.setErrorCode("1");
            response.setErrorMsg("the notification message is empty");
            return response.toString();
        }

        // 验证数据有效性
        boolean isCheckOk =
            doCheck(request.getStatusUpdateNotification(), request.getNotifycationSignature(), PUBLIC_KEY);
        if (!isCheckOk) {
            response.setErrorCode("2");
            response.setErrorMsg("verify the sign failure");
            return response.toString();
        }

        // 自定义内容实现
        StatusUpdateNotification statusUpdateNotification =
            MAPPER.readValue(request.getStatusUpdateNotification(), StatusUpdateNotification.class);
        int notificationType = statusUpdateNotification.getNotificationType();
        switch (notificationType) {
            // 订阅的第一次购买行为
            case NotificationType.INITIAL_BUY:
                break;
                // 客服或者App撤销了一个订阅
            case NotificationType.CANCEL:
                break;
                // 一个已经过期的订阅自动续期成功
            case NotificationType.RENEWAL:
                break;
                // 用户主动恢复一个已经过期的订阅
            case NotificationType.INTERACTIVE_RENEWAL:
                break;
                // 顾客选择组内其他选项并且在当前订阅到期后生效，当前周期不受影响
            case NotificationType.NEW_RENEWAL_PREF:
                break;
                // 订阅服务续期被用户、开发者或者HW停止，已经收费的服务仍然有效
            case NotificationType.RENEWAL_STOPPED:
                break;
                // 用户主动恢复了一个订阅商品，续期状态恢复正常
            case NotificationType.RENEWAL_RESTORED:
                break;
                // 表示一次续期收费成功，包括优惠、免费试用和沙箱
            case NotificationType.RENEWAL_RECURRING:
                break;
                // 表示一个已经到期的订阅进入账号保留期
            case NotificationType.ON_HOLD:
                break;
                // 顾客设置暂停续期计划后，到期后订阅进入Paused状态
            case NotificationType.PAUSED:
                break;
                // 顾客设置了暂停续期计划
            case NotificationType.PAUSE_PLAN_CHANGED:
                break;
                // 顾客同意了涨价
            case NotificationType.PRICE_CHANGE_CONFIRMED:
                break;
                // 订阅的续期时间已经延期
            case NotificationType.DEFERRED:
                break;
            default:
                break;
        }

        response.setErrorCode("0");
        response.setErrorMsg("success");
        return response.toString();
    }

    /**
     * 校验签名
     *
     * @param content content
     * @param sign sign
     * @param publicKey publicKey
     * @return boolean
     */
    public static boolean doCheck(String content, String sign, String publicKey) {
        if (StringUtils.isEmpty(sign)) {
            return false;
        }
        if (StringUtils.isEmpty(publicKey)) {
            return false;
        }
        try {
            // 选用指定的验签算法进行数据验签
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            byte[] encodedKey = Base64.decodeBase64(publicKey);
            PublicKey pubKey = keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));
            java.security.Signature signature;
            signature = java.security.Signature.getInstance("SHA256WithRSA");
            signature.initVerify(pubKey);
            signature.update(content.getBytes(StandardCharsets.UTF_8));
            byte[] bsign = Base64.decodeBase64(sign);
            return signature.verify(bsign);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
