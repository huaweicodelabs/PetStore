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

package com.huawei.hms.petstore.service.iap;

import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SubscriptionService {
    // IAP 订阅服务对应的URL
    public static final String ROOT_URL = "https://subscr-drcn.iap.hicloud.com";

    public static String getSubscription(String subscriptionId, String purchaseToken) throws Exception {
        // 获取应用级的 AccessToken
        String appAt = AtDemo.getAppAT();

        // 构造请求头
        Map<String, String> headers = AtDemo.buildAuthorization(appAt);

        // 构造请求体
        Map<String, String> bodyMap = new HashMap<>();
        bodyMap.put("subscriptionId", subscriptionId);
        bodyMap.put("purchaseToken", purchaseToken);

        String msgBody = JSONObject.toJSONString(bodyMap);

        String response = AtDemo.httpPost(ROOT_URL + "/sub/applications/v2/purchases/get",
            "application/json; charset=UTF-8", msgBody, 5000, 5000, headers);

        System.out.println(response);
        return response;
    }

    public static String stopSubscription(String subscriptionId, String purchaseToken) throws Exception {
        String appAt = AtDemo.getAppAT();
        Map<String, String> headers = AtDemo.buildAuthorization(appAt);

        Map<String, String> bodyMap = new HashMap<>();
        bodyMap.put("subscriptionId", subscriptionId);
        bodyMap.put("purchaseToken", purchaseToken);

        String msgBody = JSONObject.toJSONString(bodyMap);

        String response = AtDemo.httpPost(ROOT_URL + "/sub/applications/v2/purchases/stop",
            "application/json; charset=UTF-8", msgBody, 5000, 5000, headers);

        System.out.println(response);
        return response;
    }

    public static String delaySubscription(String subscriptionId, String purchaseToken, Long currentExpirationTime,
        Long desiredExpirationTime) throws Exception {
        String appAt = AtDemo.getAppAT();
        Map<String, String> headers = AtDemo.buildAuthorization(appAt);

        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("subscriptionId", subscriptionId);
        bodyMap.put("purchaseToken", purchaseToken);
        bodyMap.put("currentExpirationTime", currentExpirationTime);
        bodyMap.put("desiredExpirationTime", desiredExpirationTime);

        String msgBody = JSONObject.toJSONString(bodyMap);

        String response = AtDemo.httpPost(ROOT_URL + "/sub/applications/v2/purchases/delay",
            "application/json; charset=UTF-8", msgBody, 5000, 5000, headers);

        System.out.println(response);
        return response;
    }

    public static String returnFeeSubscription(String subscriptionId, String purchaseToken) throws Exception {
        String appAt = AtDemo.getAppAT();
        Map<String, String> headers = AtDemo.buildAuthorization(appAt);

        Map<String, String> bodyMap = new HashMap<>();
        bodyMap.put("subscriptionId", subscriptionId);
        bodyMap.put("purchaseToken", purchaseToken);

        String msgBody = JSONObject.toJSONString(bodyMap);

        String response = AtDemo.httpPost(ROOT_URL + "/sub/applications/v2/purchases/returnFee",
            "application/json; charset=UTF-8", msgBody, 5000, 5000, headers);

        System.out.println(response);
        return response;
    }

    public static String withdrawSubscription(String subscriptionId, String purchaseToken) throws Exception {
        String appAt = AtDemo.getAppAT();
        Map<String, String> headers = AtDemo.buildAuthorization(appAt);

        Map<String, String> bodyMap = new HashMap<>();
        bodyMap.put("subscriptionId", subscriptionId);
        bodyMap.put("purchaseToken", purchaseToken);

        String msgBody = JSONObject.toJSONString(bodyMap);

        String response = AtDemo.httpPost(ROOT_URL + "/sub/applications/v2/purchases/withdrawal",
            "application/json; charset=UTF-8", msgBody, 5000, 5000, headers);

        System.out.println(response);
        return response;
    }

}
