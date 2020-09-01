/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2019-2020. All rights reserved.
 */

package com.huawei.hms.petstore.controller.resp;

import com.huawei.hms.petstore.service.dto.PetInfo;

import lombok.Getter;
import lombok.Setter;

/**
 * DemoResp.java
 * @since 2020-02-27
 */
@Setter
@Getter
public class QueryPetResp {
    private PetInfo petInfo;
}
