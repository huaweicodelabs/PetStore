/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2019-2020. All rights reserved.
 */

package com.huawei.hms.petstore.controller.req;

import lombok.Getter;
import lombok.Setter;

/**
 * JwsVerifyReq.java
 *
 * @since 2020-03-16
 */
@Setter
@Getter
public class JwsVerifyReq {
    private String jws;
}
