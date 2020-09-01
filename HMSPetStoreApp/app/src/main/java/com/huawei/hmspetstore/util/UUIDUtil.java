/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.hmspetstore.util;

import java.util.UUID;

/**
 * 功能描述: 生成唯一键
 */
public abstract class UUIDUtil {
    /**
     * UUID
     */
    public static String getUUID() {
        return UUID.randomUUID().toString();
    }
}