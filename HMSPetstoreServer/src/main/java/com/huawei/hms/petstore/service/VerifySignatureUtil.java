/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.

 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.huawei.hms.petstore.service;

import com.huawei.hms.petstore.common.log.LogFactory;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSObject;

import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.net.ssl.SSLException;

/**
 * Description: Verify cert chain, signature and hostname
 *
 */
public class VerifySignatureUtil {

    private static final Logger RUN_LOG = LogFactory.runLog();

    private static final String CERTS_CBG_ROOT_CER = "certs/cbg_root.cer";

    private static final String SHA256_WITH_RSA = "SHA256withRSA";

    private static final String RS256 = "RS256";

    private static final String SYSINTEGRITY_PLATFORM_HOST = "sysintegrity.platform.hicloud.com";

    private static final DefaultHostnameVerifier HOSTNAME_VERIFIER = new DefaultHostnameVerifier();

    private static X509Certificate huaweiCbgRootCaCert;

    static {
        // 加载CBG根证书
        ClassPathResource resource = new ClassPathResource(CERTS_CBG_ROOT_CER);
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            huaweiCbgRootCaCert = (X509Certificate) cf.generateCertificate(resource.getInputStream());
        } catch (IOException | CertificateException e) {
            RUN_LOG.error("CBG root cert load failed!");
        }
    }

    public static boolean verifySignature(JWSObject jws) throws NoSuchAlgorithmException {
        JWSAlgorithm jwsAlgorithm = jws.getHeader().getAlgorithm();
        // 1. 验证JWS签名算法
        if (RS256.equals(jwsAlgorithm.getName())) {
            // 进行证书链校验，并根据签名算法获取 Signature 类实例，用来验证签名
            return verify(Signature.getInstance(SHA256_WITH_RSA), jws);
        }
        return false;
    }

    private static boolean verify(Signature signature, JWSObject jws) {
        // 提取JWS头部证书链信息, 并转换为合适的类型, 以便进行后续操作
        X509Certificate[] certs = extractX509CertChain(jws);
        // 2. 校验证书链
        try {
            verifyCertChain(certs);
        } catch (CertificateException | NoSuchAlgorithmException | InvalidKeyException | NoSuchProviderException
            | SignatureException e) {
            return false;
        }
        // 3. 校验签名证书(叶子证书)域名信息, 该域名固定为 "sysintegrity.platform.hicloud.com"
        try {
            HOSTNAME_VERIFIER.verify(SYSINTEGRITY_PLATFORM_HOST, certs[0]);
        } catch (SSLException e) {
            return false;
        }

        // 4. 验证JWS签名信息，使用签名证书里的公钥来验证
        PublicKey pubKey = certs[0].getPublicKey();
        try {
            // 使用签名证书里的公钥初始化 Signature 实例
            signature.initVerify(pubKey);
            // 从 JWS 提取签名输入，并输入到 Signature 实例
            signature.update(jws.getSigningInput());
            // 使用 Signature 实例来验证签名信息
            return signature.verify(jws.getSignature().decode());
        } catch (InvalidKeyException | SignatureException e) {
            return false;
        }
    }

    private static X509Certificate[] extractX509CertChain(JWSObject jws) {
        List<X509Certificate> certs = new ArrayList<>();
        List<com.nimbusds.jose.util.Base64> x509CertChain = jws.getHeader().getX509CertChain();
        try {
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            certs.addAll(x509CertChain.stream().map(cert -> {
                try {
                    return (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(cert.decode()));
                } catch (CertificateException e) {
                    RUN_LOG.error("X5c extract failed!");
                }
                return null;
            }).filter(Objects::nonNull).collect(Collectors.toList()));
        } catch (CertificateException e) {
            RUN_LOG.error("X5c extract failed!");
        }
        return (X509Certificate[]) certs.toArray();
    }

    private static void verifyCertChain(X509Certificate[] certs) throws CertificateException, NoSuchAlgorithmException,
        InvalidKeyException, NoSuchProviderException, SignatureException {
        // 逐一验证证书有效期及证书的签发关系
        for (int i = 0; i < certs.length - 1; ++i) {
            // 校验证书有效期
            certs[i].checkValidity();
            PublicKey pubKey = certs[i + 1].getPublicKey();
            certs[i].verify(pubKey);
        }
        // 使用预置的 HUAWEI CBG 根证书, 来验证证书链中的最后一张证书
        PublicKey caPubKey = huaweiCbgRootCaCert.getPublicKey();
        certs[certs.length - 1].verify(caPubKey);
    }

}
