/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2019-2020. All rights reserved.
 */

package com.huawei.hms.petstore.common.log;

import lombok.Getter;
import lombok.Setter;

/**
 * InterfaceLog.java
 * @since 2020-03-14
 */
@Setter
@Getter
public class InterfaceLog {

    private long begin;

    private String interfName;

    private Object req;

    private Object resp;

    private int errCode;

    private String errMsg;

    /**
     * 构造函数
     */
    public InterfaceLog(String interfName) {
        this.interfName = interfName;
        begin = System.currentTimeMillis();
    }
}
