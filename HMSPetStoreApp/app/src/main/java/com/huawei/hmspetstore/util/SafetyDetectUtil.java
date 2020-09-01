
package com.huawei.hmspetstore.util;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.huawei.agconnect.config.AGConnectServicesConfig;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.support.api.entity.safetydetect.SysIntegrityResp;
import com.huawei.hms.support.api.entity.safetydetect.UserDetectResponse;
import com.huawei.hms.support.api.safetydetect.SafetyDetect;
import com.huawei.hms.support.api.safetydetect.SafetyDetectStatusCodes;
import com.huawei.hmspetstore.R;
import com.huawei.hmspetstore.common.ICallBack;

import com.google.gson.Gson;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SafetyDetectUtil.java
 */
public class SafetyDetectUtil {

    public static void detectSysIntegrity(final Activity activity, final ICallBack<? super Boolean> callBack) {
        // 生成 nonce 值
        byte[] nonce = ("Sample" + System.currentTimeMillis()).getBytes(StandardCharsets.UTF_8);
        // 从app目录下的agconnect-services.json文件中读取app_id字段
        String appId = AGConnectServicesConfig.fromContext(activity).getString("client/app_id");
        // 获取 Safety Detect 服务客户端，调用sysIntegrity API，并添加成功事件监听
        SafetyDetect.getClient(activity)
                .sysIntegrity(nonce, appId)
                .addOnSuccessListener(new OnSuccessListener<SysIntegrityResp>() {
                    @Override
                    public void onSuccess(SysIntegrityResp response) {
                        // Safety Detect 服务接口成功响应。可以通过 SysIntegrityResp 类的 getResult 方法来获取检测结果
                        String jwsStr = response.getResult();
                        // 将检测结果发送至您的服务器进行验证
                        final VerifyResultHandler verifyResultHandler = new VerifyResultHandler(jwsStr, callBack);
                        verifyJws(activity, jwsStr, verifyResultHandler);
                    }

                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        String errorMsg;
                        if (e instanceof ApiException) {
                            // sysIntegrity方法存在检测失败的可能，此时会抛出ApiException异常。
                            ApiException apiException = (ApiException) e;
                            errorMsg = SafetyDetectStatusCodes.getStatusCodeString(apiException.getStatusCode()) + ": "
                                    + apiException.getMessage();
                            // 您可以通过 apiException.getStatusCode() 方法获取状态码。

                        } else {
                            // 发生未知异常，通过日志获取异常信息。
                            errorMsg = e.getMessage();
                        }
                        callBack.onError("Failed to call sysIntegrity api! Detail: " + errorMsg);
                    }
                });
    }

    private static void verifyJws(Activity activity, String jwsStr, final ICallBack<? super Boolean> callback) {
        // 构造http客户端
        Call.Factory okHttpClient =
                new OkHttpClient.Builder().connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS).build();
        // 生成请求消息体
        RequestBody body = RequestBody.create(new Gson().toJson(new VerifyJwsReq(jwsStr)),
                MediaType.get("application/json"));

        // 从配置文件中获取服务端域名
        Properties proper = getProperties(activity.getApplicationContext());
        String serviceUrl = proper.getProperty("serverUrl");
        if (TextUtils.isEmpty(serviceUrl)) {
            callback.onError("Get server url failed!");
            return;
        }

        String integrity_jws_url = activity.getString(R.string.sys_integrity_jws_verifier_url);
        // 拼接请求URL
        String sysIntegrityJwsVerifierUrl = COMPILED_SERVER_BASE_URL.matcher(integrity_jws_url)
                .replaceAll(Matcher.quoteReplacement(serviceUrl));
        // 构造请求并执行
        final Request request = new Request.Builder().url(sysIntegrityJwsVerifierUrl).post(body).build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onResponse(@NotNull Call realCall, @NotNull Response response) throws IOException {
                // 服务器验证请求执行成功，解析服务器验证结果并返回
                try (final ResponseBody responseBody = response.body()) {
                    final String content = responseBody.string();
                    final JSONObject jsonObject = new JSONObject(content);
                    callback.onSuccess(jsonObject.getBoolean("result"));
                } catch (JSONException e) {
                    // 服务器验证结果解析失败
                    callback.onError("Verify sysIntegrity result failed!");
                }
            }

            @Override
            public void onFailure(@NotNull Call realCall, @NotNull IOException e) {
                // 服务器验证请求执行失败
                callback.onError("Verify sysIntegrity result failed! Detail: " + e.getMessage());
            }
        });
    }

    public static Properties getProperties(Context context) {
        Properties props = new Properties();
        try (InputStream in = context.getAssets().open("app_config")) {
            props.load(in);
        } catch (IOException e1) {
            Log.i(TAG, "get properties exception:");
        }

        return props;
    }

    public static void callUserDetect(final Activity activity, final ICallBack<? super Boolean> callBack) {
        Log.i(TAG, "User detection start.");
        // 从app目录下的agconnect-services.json文件中读取app_id字段
        String appId = AGConnectServicesConfig.fromContext(activity).getString("client/app_id");
        // 调用虚假用户检测 API，并添加回调来做后续的异步处理
        SafetyDetect.getClient(activity)
                .userDetection(appId)
                .addOnSuccessListener(new OnSuccessListener<UserDetectResponse>() {
                    @Override
                    public void onSuccess(UserDetectResponse userDetectResponse) {
                        // 虚假用户检测成功，通过 getResponseToken 方法来获取 responseToken
                        final String responseToken = userDetectResponse.getResponseToken();
                        Log.i(TAG, MessageFormat.format("User detection succeed, response={0}", responseToken));
                        // 将该 responseToken 发送到您的服务器，然后在你的服务器上调用 HMS Core 的云侧 API，
                        // 来获取虚假用户检测结果
                        boolean verifyResult = verifyUserRisks(activity, responseToken);
                        callBack.onSuccess(verifyResult);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        // 虚假用户检测失败，您可以根据具体的错误码进行处理
                        String errorMsg;
                        if (e instanceof ApiException) {
                            // HMS API包含一些详细错误信息。
                            // 您可以使用apiException.getStatusCode()方法获取状态码。
                            ApiException apiException = (ApiException) e;
                            errorMsg = SafetyDetectStatusCodes.getStatusCodeString(apiException.getStatusCode()) + ": "
                                    + apiException.getMessage();
                        } else {
                            errorMsg = e.getMessage();
                        }
                        Log.i(TAG, "User detection fail. Error info: " + errorMsg);
                        callBack.onError(errorMsg);
                    }
                });
    }

    // 将 responseToken 发送到您的服务器，然后在服务器上调用 HMS Core 的云侧 API，来获取虚假用户检测结果
    private static boolean verifyUserRisks(final Activity activity, final String responseToken) {
        // 从配置文件中获取服务端域名
        Properties proper = getProperties(activity.getApplicationContext());
        String serviceUrl = proper.getProperty("serverUrl");
        if (TextUtils.isEmpty(serviceUrl)) {
            return false;
        }
        String verifier_url = activity.getString(R.string.user_detection_user_risk_verifier_url);
        // 拼接请求URL
        final String userRisksVerifierUrl = COMPILED_SERVER_BASE_URL.matcher(verifier_url)
                        .replaceAll(Matcher.quoteReplacement(serviceUrl));

        try {
            return new AsyncTask<String, Void, Boolean>() {
                @Override
                protected Boolean doInBackground(String... strings) {
                    String input = strings[0];
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("response", input);
                        // 把响应发送到宠物商城 APP 服务端以获取检测结果
                        String result = sendPostMessage(userRisksVerifierUrl, jsonObject);
                        JSONObject resultJson = new JSONObject(result);
                        boolean success = resultJson.getBoolean("success");
                        // 如果结果success为true，则代表用户是真实用户，而不是机器；反之即反。
                        Log.i(TAG, "verifyUserRisks: result=" + result + ", response=" + input);
                        return success;
                    } catch (Exception e) {
                        // 在服务端获取检测结果失败
                        Log.e(TAG, "verifyUserRisks doInBackground failed!");
                        return false;
                    }
                }
            }.execute(responseToken).get();
        } catch (ExecutionException | InterruptedException e) {
            // 从服务端获取检测结果失败
            Log.e(TAG, "verifyUserRisks failed!");
            return false;
        }
    }

    /**
     * 发送 post 消息
     */
    private static String sendPostMessage(String baseUrl, JSONObject postDataParams) throws IOException {
        URL url = new URL(baseUrl);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(20000);
        conn.setConnectTimeout(20000);
        conn.setRequestMethod("POST");
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");

        try (OutputStream os = conn.getOutputStream();
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8))) {
            writer.write(postDataParams.toString());
            writer.flush();
        }

        int responseCode = conn.getResponseCode(); // To Check for 200
        if (responseCode == HttpURLConnection.HTTP_OK) {
            InputStreamReader reader = new InputStreamReader(conn.getInputStream());
            BufferedReader in = new BufferedReader(reader);
            StringBuffer sb = new StringBuffer();
            String line;
            while ((line = in.readLine()) != null) {
                sb.append(line);
                break;
            }
            in.close();
            reader.close();
            return sb.toString();
        }
        return null;
    }

    private static final String TAG = "SafetyDetectUtil";

    private static final Pattern COMPILED_SERVER_BASE_URL = Pattern.compile("<baseurl>", Pattern.LITERAL);

    private static final long CONNECT_TIMEOUT = 2L; // second

    private static final class VerifyResultHandler implements ICallBack<Boolean> {
        private final String jwsStr;

        private final ICallBack<? super Boolean> callBack;

        private VerifyResultHandler(String jwsStr, ICallBack<? super Boolean> callBack) {
            this.jwsStr = jwsStr;
            this.callBack = callBack;
        }

        @Override
        public void onSuccess(Boolean verified) {
            if (verified) {
                // 服务器侧验证通过，提取系统完整性检测结果
                String[] jwsSplit = jwsStr.split("\\.");
                String jwsPayloadStr = jwsSplit[1];
                String payloadDetail =
                        new String(Base64.decode(jwsPayloadStr.getBytes(StandardCharsets.UTF_8), Base64.URL_SAFE),
                                StandardCharsets.UTF_8);
                try {
                    final boolean basicIntegrity = new JSONObject(payloadDetail).getBoolean("basicIntegrity");
                    // 通过回调返回系统完整性检测结果
                    callBack.onSuccess(basicIntegrity);
                } catch (JSONException e) {
                    callBack.onError("SysIntegrity result verify failed! Detail: ");
                }
            } else {
                callBack.onError("SysIntegrity result verify failed!");
            }
        }

        @Override
        public void onError(String errorMsg) {
            callBack.onError(errorMsg);
        }
    }

    private static class VerifyJwsReq {
        private String jws;

        public VerifyJwsReq(String jwsStr) {
            jws = jwsStr;
        }
    }
}