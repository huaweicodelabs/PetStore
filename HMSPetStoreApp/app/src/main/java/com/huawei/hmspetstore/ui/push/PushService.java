/*
 *  Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.huawei.hmspetstore.ui.push;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.huawei.agconnect.config.AGConnectServicesConfig;
import com.huawei.hmf.tasks.OnCompleteListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.aaid.HmsInstanceId;
import com.huawei.hms.push.HmsMessageService;
import com.huawei.hms.push.HmsMessaging;
import com.huawei.hms.push.RemoteMessage;
import com.huawei.hmspetstore.ui.center.MemberRight;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.huawei.hmspetstore.ui.push.PushConst.TAG;
import static com.huawei.hmspetstore.util.SafetyDetectUtil.getProperties;

/**
 * 功能描述
 */
public class PushService extends HmsMessageService {
    private static final String MESSAGE_FILTER_VIP = "vip";
    private static final String MESSAGE_TITLE = "title";
    private static final String MESSAGE_CONTENT = "content";

    private static final String APPID_PATH = "client/app_id";

    private static final String HCM = "HCM";

    public static void init(final Context context) {
        getToken(context);
    }

    private static void getToken(final Context context) {
        // get push token
        new Thread() {
            @Override
            public void run() {
                try {
                    String appId = AGConnectServicesConfig.fromContext(context).getString(APPID_PATH);
                    String pushToken = HmsInstanceId.getInstance(context).getToken(appId, HCM);
                    if(!TextUtils.isEmpty(pushToken)) {
                        Log.i(TAG, "Push Token:" + pushToken);
                        uploadToken(context, pushToken);
                    }
                } catch (Exception e) {
                    Log.e(TAG,"getToken failed, Exception");
                }
            }
        }.start();
    }

    @Override
    public void onNewToken(String pushToken) {
        Log.i(TAG, "Push Token:" + pushToken);
        uploadToken(getApplicationContext(), pushToken);
    }

    private static void uploadToken(Context context, String pushToken) {
        Call.Factory okHttpClient = new OkHttpClient.Builder().connectTimeout(2L, TimeUnit.SECONDS).build();
        RequestBody body = RequestBody.create(pushToken, MediaType.get("application/octet-stream"));

        Properties proper = getProperties(context.getApplicationContext());
        String serviceUrl = proper.getProperty("serverUrl");
        if (TextUtils.isEmpty(serviceUrl)) {
            Log.e(TAG, "get server url failed");
            return;
        }
        final Request request = new Request.Builder().url(serviceUrl + "/petstore/newToken").post(body).build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.e(TAG, "upload token failed, Exception: " + e.toString());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.code() == 200) {
                    Log.i(TAG, "upload token success");
                } else {
                    Log.e(TAG, "upload token failed, code:" + response.code() + " message:" + response.message());
                }
            }
        });
    }

    @Override
    public void onMessageReceived(RemoteMessage message) {
        Context context = getApplicationContext();
        String switchStr = PushSharedPreferences.readConfig(context, PushConst.PUSH_MESSAGE_SWITCH);
        if ((!TextUtils.isEmpty(switchStr)) && (!Boolean.parseBoolean(switchStr))) {
            Log.w(TAG, "Push message switch is Off");
            return;
        }

        Map<String, String> data = message.getDataOfMap();

        if (data.containsKey(MESSAGE_FILTER_VIP)) {
            if (!getUserType().equals(data.get(MESSAGE_FILTER_VIP))) {
                Log.i(TAG, "vip type is not equal");
                return;
            }
        }
        PushSharedPreferences.saveMessage(context, data.get(MESSAGE_TITLE) + PushConst.PUSH_SPLIT + getDate(), data.get(MESSAGE_CONTENT));
    }

    private String getDate() {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd");
        Date date = new Date(System.currentTimeMillis());
        return simpleDateFormat.format(date);
    }

    private String getUserType() {
        Context context = getApplicationContext();
        return MemberRight.getMemberType(context);
    }

    public static void turnOnOff(final Context context, final Boolean isTurnOn) {
        new Thread() {
            @Override
            public void run() {
                try {
                    if (isTurnOn) {
                        HmsMessaging.getInstance(context).turnOnPush();
                    } else {
                        HmsMessaging.getInstance(context).turnOffPush();
                    }
                } catch (Exception e){
                    Log.e(TAG, "turn " + (isTurnOn ? "On" : "Off") + " failed, Exception: " );
                }
            }
        }.start();
    }

    public static void subscribe(final Context context, final String topic) {
        if (isSubscribed(context, topic)) {
            return;
        }
        new Thread() {
            @Override
            public void run() {
                try {
                    HmsMessaging.getInstance(context).subscribe(topic).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(Task<Void> task) {
                            boolean isSuccessful = task.isSuccessful();
                            Log.i(TAG, "subscribe " + topic + " " + (isSuccessful ? "success" : "failed, Exception: " + task.getException().toString()));
                            if (isSuccessful) {
                                PushSharedPreferences.saveTopic(context, topic);
                            }
                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG, "subscribe " + topic + " failed, Exception: ");
                }
            }
        }.start();
    }

    private static boolean isSubscribed(Context context, String topic) {
        Map<String, String> topics = PushSharedPreferences.readTopic(context);
        return topics.containsKey(topic);
    }

    public static void unsubscribe(final Context context, final String topic) {
        new Thread() {
            @Override
            public void run() {
                try {
                    HmsMessaging.getInstance(context).unsubscribe(topic).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(Task<Void> task) {
                            Log.i(TAG, "unsubscribe " + topic + " " + (task.isSuccessful() ? "success" : "failed, Exception: " + task.getException().toString()));
                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG, "unsubscribe " + topic + " failed, Exception: " );
                }
            }
        }.start();
    }
}