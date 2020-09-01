/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2019-2020. All rights reserved.
 */

package com.huawei.hms.petstore.service;

import com.huawei.hms.petstore.common.log.LogFactory;
import com.huawei.hms.petstore.controller.req.JwsVerifyReq;
import com.huawei.hms.petstore.controller.req.UserRisksVerifyReq;
import com.huawei.hms.petstore.controller.resp.JwsVerifyResp;
import com.huawei.hms.petstore.dao.PetMapper;
import com.huawei.hms.petstore.dao.domain.Pet;
import com.huawei.hms.petstore.service.dto.PetInfo;

import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.util.Base64URL;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.text.ParseException;

/**
 * PetService.java
 * @since 2020-03-13
 */
@Service
public class PetService {
    private static final Logger RUN_LOG = LogFactory.runLog();

    private static final int JWS_PART_COUNT = 3;

    private static final String JWS_PART_SEP = "\\.";

    @Autowired
    private PetMapper petMapper;

    /**
     * get pet by id
     * @param id id
     * @return pet information
     */
    public PetInfo getPet(Long id) {
        Pet pet = petMapper.getPetByID(id);
        PetInfo pefInfo = new PetInfo();
        BeanUtils.copyProperties(pet, pefInfo);
        return pefInfo;
    }

    /**
     * 系统完整性检测结果jws验签
     * @param  jwsVerifyReq jwsVerifyReq
     * @return verify result, true:pass, false:fail
     */
    public JwsVerifyResp verifyJws(JwsVerifyReq jwsVerifyReq) {
        JwsVerifyResp jwsVerifyResp = new JwsVerifyResp();
        jwsVerifyResp.setResult(Boolean.FALSE);
        // 获取端侧发送到服务器侧的jws信息
        String jwsStr = jwsVerifyReq.getJws();
        if (StringUtils.isBlank(jwsStr)) {
            return jwsVerifyResp;
        }
        // 解析JWS, 分段, 该JWS固定为三段，使用"."号分隔
        String[] jwsSplit = jwsStr.split(JWS_PART_SEP);
        if (jwsSplit.length != JWS_PART_COUNT) {
            return jwsVerifyResp;
        }

        try {
            // 解析JWS, Base64解码, 并构造JWSObject
            JWSObject jwsObject =
                new JWSObject(new Base64URL(jwsSplit[0]), new Base64URL(jwsSplit[1]), new Base64URL(jwsSplit[2]));
            // 验证JWS并设置验证结果
            boolean result = VerifySignatureUtil.verifySignature(jwsObject);
            jwsVerifyResp.setResult(result);
        } catch (ParseException | NoSuchAlgorithmException e) {
            RUN_LOG.catching(e);
        }
        return jwsVerifyResp;
    }

    /**
     * 虚假用户检测结果验证
     * @param  userRisksVerifyReq userRisksVerifyReq
     * @return verify result
     */
    public String verifyUserRisks(UserRisksVerifyReq userRisksVerifyReq) {
        return new UserRisksVerifyHandler().handle(userRisksVerifyReq);
    }
}
