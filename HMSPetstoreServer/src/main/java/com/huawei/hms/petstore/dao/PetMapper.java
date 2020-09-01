/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2019-2020. All rights reserved.
 */

package com.huawei.hms.petstore.dao;

import com.huawei.hms.petstore.dao.domain.Pet;

/**
 * operate database demo
 * @since 2020-03-13
 */
public interface PetMapper {

    /**
     * get Pet by id
     * @param id pet id
     * @return pet information
     */
    Pet getPetByID(Long id);
}
