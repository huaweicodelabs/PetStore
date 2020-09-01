/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2019-2020. All rights reserved.
 */

package com.huawei.hms.petstore.service.push;

import com.huawei.hms.petstore.common.log.LogFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;

public class HttpsUtil {
    private static final Logger Log = LogFactory.runLog();

    private static final String GET_AT_URL = "https://oauth-login.cloud.huawei.com/oauth2/v2/token";

    private static final String SEND_PUSH_URL = "https://push-api.cloud.huawei.com/v1/[appid]/messages:send";

    private static final String RMS_VERIFY_URL = "https://rms-drcn.platform.dbankcloud.com/rms/v1/userRisks/verify";

    private static final int SUCCESS_CODE = 200;

    public static String getAccessToken(String appId, String appSecret) {
        RestTemplate restTemplate = new RestTemplate();
        StringBuilder params = new StringBuilder();
        params.append("grant_type=client_credentials");
        params.append("&client_id=").append(appId);
        params.append("&client_secret=").append(appSecret);
        String response = null;
        try {
            response = restTemplate.postForObject(new URI(GET_AT_URL), params.toString(),
                    String.class);
        } catch (Exception e) {
            Log.catching(e);
        }
        if (!StringUtils.isEmpty(response)) {
            JSONObject jsonObject = JSONObject.parseObject(response);
            return jsonObject.getString("access_token").replace("\\", "");
        }
        return "";
    }

    public static JSONObject sendPushMessage(String appId, String appSecret, JSONObject messageBody) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken(appId, appSecret));
        String response = null;
        HttpEntity<Object> httpEntity = new HttpEntity<Object>(messageBody, headers);
        try {
            String uri = SEND_PUSH_URL.replace("[appid]", appId);
            Log.debug(httpEntity.toString());
            response = restTemplate.postForObject(new URI(uri), httpEntity, String.class);
        } catch (Exception e) {
            Log.catching(e);
        }
        //
        JSONObject jsonObject = null;
        if (!StringUtils.isEmpty(response)) {
            jsonObject = JSONObject.parseObject(response);
        }
        return jsonObject;
    }

    public static String sendRmsMessage(String appId, String appSecret, String responseToken) {
        // 构造 HttpPost 请求对象
        HttpPost httpPostRequest;
        // 拼接 HMS User Detect Server 服务端接口相关的 URI
        URI uri = buildUri(appId);
        httpPostRequest = new HttpPost(uri);
        httpPostRequest.addHeader("content-type", "application/json");
        StringEntity entityData;
        try {
            // 填充消息体
            JSONObject messageObject = new JSONObject();
            // 调用方法来获取 Access Token，并填充到消息体里
            messageObject.put("accessToken", applyAccessToken(appId, appSecret));
            // 将 responseToken 填充到消息体里
            messageObject.put("response", responseToken);
            entityData = new StringEntity(messageObject.toString());
        } catch (UnsupportedEncodingException e) {
            Log.error("fail to new StringEntity, msg:{}, class:{}", e.getMessage(), e.getClass().getSimpleName());
            return "";
        }
        httpPostRequest.setEntity(entityData);
        // 执行 http post 请求
        return execute(httpPostRequest);
    }

    private static URI buildUri(String appId) {
        URIBuilder uriBuilder;
        URI uri = null;
        try {
            // HMS User Detect Server 服务端接口相关的 URL
            uriBuilder = new URIBuilder(RMS_VERIFY_URL);
            // 添加参数 APP ID
            uriBuilder.addParameter("appId", appId);
            uri = uriBuilder.build();
        } catch (URISyntaxException e) {
            Log.error("fail to create URI, msg:{}, class:{}", e.getMessage(), e.getClass().getSimpleName());
        }
        return uri;
    }

    private static String applyAccessToken(String appId, String secretKey) {
        // 构造 HttpPost 请求对象
        HttpPost httpPostRequest = new HttpPost(GET_AT_URL);
        // 设置内容类型
        httpPostRequest.setHeader("content-type", "application/x-www-form-urlencoded");
        // 填充消息体
        List<NameValuePair> entityData = new ArrayList<>();
        entityData.add(new BasicNameValuePair("grant_type", "client_credentials"));
        entityData.add(new BasicNameValuePair("client_id", appId));
        entityData.add(new BasicNameValuePair("client_secret", secretKey));
        UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(entityData, StandardCharsets.UTF_8);
        httpPostRequest.setEntity(urlEncodedFormEntity);
        // 执行 http post 请求
        String response = execute(httpPostRequest);
        // 返回从响应消息体里提取的 Access Token
        return JSON.parseObject(response).get("access_token").toString();
    }

    private static String execute(HttpPost httpPostRequest) {
        SSLContext sslcontext = null;
        try {
            sslcontext = SSLContexts.custom().loadTrustMaterial(null, new TrustStrategy() {
                @Override
                public boolean isTrusted(X509Certificate[] chain, String authType) {
                    return true;
                }
            }).build();
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            Log.error("fail to build ssl context, msg:{}, class:{}", e.getMessage(), e.getClass().getSimpleName());
        }
        SSLConnectionSocketFactory sslConnectionSocketFactory =
            new SSLConnectionSocketFactory(sslcontext, null, null, new NoopHostnameVerifier());

        HttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslConnectionSocketFactory).build();
        HttpResponse httpResponse;
        try {
            httpResponse = httpClient.execute(httpPostRequest);
        } catch (IOException e) {
            Log.error("fail to create HttpResponse, msg:{}, class:{}", e.getMessage(), e.getClass().getSimpleName());
            return new JSONObject().toString();
        }

        String responseContent = "";
        if (httpResponse.getStatusLine().getStatusCode() == SUCCESS_CODE) {
            org.apache.http.HttpEntity httpEntity = httpResponse.getEntity();
            if (httpEntity != null) {
                try {
                    responseContent = EntityUtils.toString(httpEntity, "UTF-8");
                } catch (IOException e) {
                    Log.error("fail to get the entity content as a String, msg:{}, class:{}", e.getMessage(),
                        e.getClass().getSimpleName());
                }
                try {
                    EntityUtils.consume(httpEntity);
                } catch (IOException e) {
                    Log.error("fail to consume HttpEntity, msg:{}, class:{}", e.getMessage(),
                        e.getClass().getSimpleName());
                }
            }
        }
        return responseContent;
    }

}
