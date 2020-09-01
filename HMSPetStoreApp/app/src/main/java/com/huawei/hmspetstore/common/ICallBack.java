/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.hmspetstore.common;

/**
 * 功能描述: 通用回调接口.
 */
public interface ICallBack<T> {

    /**
     * onSuccess
     *
     * @param bean bean
     */
    void onSuccess(T bean);

    /**
     * onError
     *
     * @param errorMsg errorMsg
     */
    void onError(String errorMsg);
}