/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2019-2020. All rights reserved.
 */

package com.huawei.hms.petstore.common.log;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * LogFactory.java
 * @since 2020-03-14
 */
public class LogFactory {
    
    /**
     * runtime log instance
     * @return log instance
     */
    public static Logger runLog() {
        return LogManager.getLogger("RUN");
    }

    /**
     * interface log instance
     * @return log instance
     */
    public static Logger interfaceLog() {
        return LogManager.getLogger("INTERFACE");
    }
}
