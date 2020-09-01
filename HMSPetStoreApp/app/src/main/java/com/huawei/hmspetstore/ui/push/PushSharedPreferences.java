package com.huawei.hmspetstore.ui.push;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;

/**
 * 功能描述
 */
public abstract class PushSharedPreferences {
    private static final String PUSH_SP_TOPIC = "Push_Topic";
    private static final String PUSH_SP_MESSAGE = "Push_Message";
    private static final String PUSH_SP_CONFIG = "Push_Config";

    public static void saveTopic(Context context, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PUSH_SP_TOPIC, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, "");
        editor.apply();
    }

    public static Map<String, String> readTopic(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PUSH_SP_TOPIC, Context.MODE_PRIVATE);
        return (Map<String, String>) sharedPreferences.getAll();
    }

    public static void clearTopic(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PUSH_SP_TOPIC, Context.MODE_PRIVATE);
        sharedPreferences.edit().clear().apply();
    }

    public static void saveMessage(Context context, String key, String value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PUSH_SP_MESSAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static Map<String, String> readMessage(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PUSH_SP_MESSAGE, Context.MODE_PRIVATE);
        return (Map<String, String>) sharedPreferences.getAll();
    }

    public static void clearMessage(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PUSH_SP_MESSAGE, Context.MODE_PRIVATE);
        sharedPreferences.edit().clear().apply();
    }

    public static void saveConfig(Context context, String key, String value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PUSH_SP_CONFIG, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String readConfig(Context context, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PUSH_SP_CONFIG, Context.MODE_PRIVATE);
        Map<String, ?> map = sharedPreferences.getAll();
        return (String) map.get(key);
    }
}