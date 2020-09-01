/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.hmspetstore.constant;

import com.huawei.hms.site.api.model.Coordinate;

/**
 * Created
 */
public interface SPConstants {

    // 是否已登录
    String KEY_LOGIN = "isLogin";
    // 是否是华为账号登录
    String KEY_HW_LOGIN = "isHwLogin";
    // 华为账号-openId
    String KEY_HW_OEPNID = "openId";
    // 本地用户
    String KEY_LOCAL_USER = "local_user";
    // 头像
    String KEY_HEAD_PHOTO = "head_photo";
    // 昵称
    String KEY_NICK_NAME = "nick_name";
    //保存地址
    String TAG_ADDRESS_LIST = "addressList";
    // 指纹登录开关
    String FINGER_PRINT_LOGIN_SWITCH = "finger_print_login_switch";
    String BUNDLE_DATA = "data";
    Coordinate COORDINATE = new Coordinate(52.541327, 1.371009);
    Coordinate COORDINATE_BICYCLING = new Coordinate(51.541327, -1.071009);
    Coordinate COORDINATE_WALKING = new Coordinate(51.641327, -0.071009);
    int VALUE_1 = 1;
    int VALUE_2 = 2;
}