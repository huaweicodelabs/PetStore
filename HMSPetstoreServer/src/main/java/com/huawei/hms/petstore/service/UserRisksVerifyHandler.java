/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2019-2020. All rights reserved.
 */

package com.huawei.hms.petstore.service;

import com.huawei.hms.petstore.controller.req.UserRisksVerifyReq;
import com.huawei.hms.petstore.service.push.HttpsUtil;

/**
 * UserRisksVerifyHandler.java
 *
 * @since 2020-04-08
 */
public class UserRisksVerifyHandler {
    private static final String APP_ID = "";

    private static final String SECRET_KEY = "";

    public String handle(UserRisksVerifyReq userRisksVerifyReq) {
        // 将虚假用户检测结果发送到 HMS User Detect Server 服务端， 以获取检测结果
        return HttpsUtil.sendRmsMessage(APP_ID, SECRET_KEY, userRisksVerifyReq.getResponse());
    }
}
