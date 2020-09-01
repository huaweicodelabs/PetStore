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

public class OrderService {
    // IAP 服务对应的URL
    public static final String ROOT_URL = "https://orders-drcn.iap.hicloud.com";

    public static String verifyToken(String purchaseToken, String productId) throws Exception {
        // 获取应用级 AccessToken，实际场景无需每次发起请都获取，应该在首次或者返回401时才去更新AccessToken
        String appAt = AtDemo.getAppAT();

        // 构造请求头
        Map<String, String> headers = AtDemo.buildAuthorization(appAt);

        // 构造请求体
        Map<String, String> bodyMap = new HashMap<>();
        bodyMap.put("purchaseToken", purchaseToken);
        bodyMap.put("productId", productId);

        String msgBody = JSONObject.toJSONString(bodyMap);

        String response = AtDemo.httpPost(ROOT_URL + "/applications/purchases/tokens/verify",
            "application/json; charset=UTF-8", msgBody, 5000, 5000, headers);

        System.out.println(response);
        return response;
    }

    public static void cancelledListPurchase(Long endAt, Long startAt, Integer maxRows, Integer type,
                                             String continuationToken) throws Exception {
        String appAt = AtDemo.getAppAT();
        Map<String, String> headers = AtDemo.buildAuthorization(appAt);

        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("endAt", endAt);
        bodyMap.put("startAt", startAt);
        bodyMap.put("maxRows", maxRows);
        bodyMap.put("type", type);
        bodyMap.put("continuationToken", continuationToken);

        String msgBody = JSONObject.toJSONString(bodyMap);

        String response = AtDemo.httpPost(ROOT_URL + "/applications/v2/purchases/cancelledList",
                "application/json; charset=UTF-8", msgBody, 5000, 5000, headers);
        System.out.println(response);
    }
}
