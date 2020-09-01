/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2019-2020. All rights reserved.
 */

package com.huawei.hms.petstore.common.log;

import org.apache.commons.lang3.time.DateFormatUtils;

import com.huawei.hms.petstore.common.constant.Constant;

/**
 * InterfaceLogUtil.java
 * @since 2020-03-14
 */
public class InterfaceLogUtil {

    /**
     * Log interface log，just demo
     * @param interfaceLog interfaceLog
     */
    public static void printInterface(InterfaceLog interfaceLog) {
        StringBuilder sb = new StringBuilder();
        long end = System.currentTimeMillis();
        sb.append(Thread.currentThread().getName())
            .append("|")
            .append(interfaceLog.getInterfName())
            .append("|")
            .append(DateFormatUtils.format(interfaceLog.getBegin(), Constant.NORMAL_DATE_FORMAT))
            .append("|")
            .append(DateFormatUtils.format(end, Constant.NORMAL_DATE_FORMAT))
            .append("|")
            .append(end - interfaceLog.getBegin());
        LogFactory.interfaceLog().info(sb.toString());
    }
}
