/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package com.huawei.hms.petstore.service.iap;

import com.alibaba.fastjson.JSONObject;

import org.apache.commons.codec.binary.Base64;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

public class AtDemo {
    // APP Secret
    private static final String appSecret = ""; // your client secret

    //  APP ID
    private static final String appId = ""; // your app id

    // 验证token url
    private static final String tokenUrl = "https://oauth-login.cloud.huawei.com/oauth2/v2/token";

    private static String accessToken;

    /**
     * 获取应用级的 AccessToken.
     *
     * @return the App Level AccessToken
     * @throws Exception the exception
     */
    public static String getAppAT() throws Exception {
        // 构造请求体
        String grant_type = "client_credentials";
        String msgBody = MessageFormat.format("grant_type={0}&client_secret={1}&client_id={2}", grant_type,
            URLEncoder.encode(appSecret, "UTF-8"), appId);

        // 发送请求
        String response =
            httpPost(tokenUrl, "application/x-www-form-urlencoded; charset=UTF-8", msgBody, 5000, 5000, null);

        // 解析出AccessToken
        JSONObject obj = JSONObject.parseObject(response);
        accessToken = obj.getString("access_token");

        System.out.println(accessToken);
        return accessToken;
    }

    /**
     * 构建认证请求头
     *
     * @param appAt appAt
     * @return headers
     */
    public static Map<String, String> buildAuthorization(String appAt) {
        String oriString = MessageFormat.format("APPAT:{0}", appAt);
        String authorization =
            MessageFormat.format("Basic {0}", Base64.encodeBase64String(oriString.getBytes(StandardCharsets.UTF_8)));
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", authorization);
        headers.put("Content-Type", "application/json; charset=UTF-8");
        return headers;
    }

    /**
     * 发起post请求
     *
     * @param httpUrl        http url
     * @param data           data
     * @param connectTimeout connect timeout
     * @param readTimeout    read timeout
     * @param headers        headers
     * @return the response as string
     * @throws IOException the io exception
     */
    public static String httpPost(String httpUrl, String contentType, String data, int connectTimeout, int readTimeout,
        Map<String, String> headers) throws IOException {
        OutputStream output = null;
        InputStream in = null;
        HttpsURLConnection urlConnection = null;
        BufferedReader bufferedReader = null;
        InputStreamReader inputStreamReader = null;

        // 这里暂不做证书校验事项
        try {
            TrustManager[] trustManagers = new TrustManager[]{
                    new X509TrustManager() {
                        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                        }

                        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                        }

                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }
                    }
            };
            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(null,trustManagers,null);

            URL url = new URL(httpUrl);
            urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setSSLSocketFactory(ctx.getSocketFactory());
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.setRequestProperty("Content-Type", contentType);
            if (headers != null) {
                for (String key : headers.keySet()) {
                    urlConnection.setRequestProperty(key, headers.get(key));
                }
            }
            urlConnection.setConnectTimeout(connectTimeout);
            urlConnection.setReadTimeout(readTimeout);
            urlConnection.connect();

            // POST 数据
            output = urlConnection.getOutputStream();
            output.write(data.getBytes("UTF-8"));
            output.flush();

            // 读取响应
            if (urlConnection.getResponseCode() < 400) {
                in = urlConnection.getInputStream();
            } else {
                in = urlConnection.getErrorStream();
            }

            inputStreamReader = new InputStreamReader(in, "UTF-8");
            bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder strBuf = new StringBuilder();
            String str;
            while ((str = bufferedReader.readLine()) != null) {
                strBuf.append(str);
            }
            return strBuf.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } finally {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (inputStreamReader != null) {
                inputStreamReader.close();
            }
            if (in != null) {
                in.close();
            }
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return "error";
    }
}
