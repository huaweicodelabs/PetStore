package com.huawei.hmspetstore.util;


import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 功能描述
 * 简单的日期转换工具
 */
public class DateUtil {

    public static final String DATE_FORMAT_MM_DD = "MM-dd";

    /**
     * @return 获取当前格式化日期
     */
    public static String getCurrentFormatDate() {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_MM_DD);
        return sdf.format(new Date());
    }
}