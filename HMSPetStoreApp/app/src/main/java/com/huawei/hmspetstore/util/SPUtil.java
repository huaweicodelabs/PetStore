/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.hmspetstore.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 功能描述: 安全SP读写类.
 */
public class SPUtil {
    /**
     * 保存在手机里面的文件名
     */
    public static final String FILE_NAME = "petstoreapp_data";

    private static final String TAG = "SPUtil";

    /**
     * 保存数据的方法，我们需要拿到保存数据的具体类型，然后根据类型调用不同的保存方法
     *
     * @param context context
     * @param key     key
     * @param object  object
     */
    public static void put(Context context, String key, Object object) {
        put(context, FILE_NAME, key, object);
    }

    /**
     * 指定SharedPreferences文件名字的保存方法
     *
     * @param context context
     * @param spName  spName
     * @param key     key
     * @param object  object
     */
    public static void put(Context context, String spName, String key, Object object) {
        if (null == context) {
            Log.e(TAG, "put context is null");
            return;
        }
        SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = sp.edit();

        if (object instanceof String) {
            editor.putString(key, (String) object);
        } else if (object instanceof Integer) {
            editor.putInt(key, (Integer) object);
        } else if (object instanceof Boolean) {
            editor.putBoolean(key, (Boolean) object);
        } else if (object instanceof Float) {
            editor.putFloat(key, (Float) object);
        } else if (object instanceof Long) {
            editor.putLong(key, (Long) object);
        } else {
            editor.putString(key, object.toString());
        }

        SharedPreferencesCompat.apply(editor);
    }


    public static <T> void saveDataList(Context context,String tag, List<T> addressList) {
        if (null == addressList)
            return;
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME, Activity.MODE_PRIVATE);
        Gson gson = new Gson();
        String jsonStr=gson.toJson(addressList); //将List转换成Json
        SharedPreferences.Editor editor = sp.edit() ;
        editor.putString(tag, jsonStr) ; //存入json串
        editor.commit() ;
    }


    public static <T> List<T> getDataList(Context context,String tag, Class<T> cls) {
        List<T> datalist=new ArrayList<T>();
        SharedPreferences  preferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        String strJson = preferences.getString(tag, null);
        if (null == strJson) {
            return datalist;
        }
        Log.d(TAG,"getDataList, json:"+strJson);
        try {
            Gson gson = new Gson();
            JsonArray array = new JsonParser().parse(strJson).getAsJsonArray();
            for (JsonElement jsonElement : array) {
                datalist.add(gson.fromJson(jsonElement, cls));
            }
        } catch (Exception e) {
            Log.e(TAG,"getDataList Exception");
        }
        return datalist;
    }



    /**
     * 得到保存数据的方法，我们根据默认值得到保存的数据的具体类型，然后调用相对于的方法获取值
     *
     * @param context       context
     * @param key           key
     * @param defaultObject defaultObject
     * @return Object
     */
    public static Object get(Context context, String key, Object defaultObject) {
        return get(context, FILE_NAME, key, defaultObject);
    }

    /**
     * 得到保存数据的方法，我们根据默认值得到保存的数据的具体类型，然后调用相对于的方法获取值
     *
     * @param context       context
     * @param spName        spName
     * @param key           key
     * @param defaultObject defaultObject
     * @return Object
     */
    public static Object get(Context context, String spName, String key, Object defaultObject) {
        if (null == context) {
            Log.e(TAG, "get context is null");
            return defaultObject;
        }
        SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);

        if (defaultObject instanceof String) {
            return sp.getString(key, (String) defaultObject);
        } else if (defaultObject instanceof Integer) {
            return sp.getInt(key, (Integer) defaultObject);
        } else if (defaultObject instanceof Boolean) {
            return sp.getBoolean(key, (Boolean) defaultObject);
        } else if (defaultObject instanceof Float) {
            return sp.getFloat(key, (Float) defaultObject);
        } else if (defaultObject instanceof Long) {
            return sp.getLong(key, (Long) defaultObject);
        } else {
            return defaultObject;
        }
    }

    /**
     * 移除某个key值已经对应的值
     *
     * @param context context
     * @param key     key
     */
    public static void remove(Context context, String key) {
        remove(context, FILE_NAME, key);
    }

    /**
     * 移除某个key值已经对应的值
     *
     * @param context context
     * @param spName  spName
     * @param key     key
     */
    public static void remove(Context context, String spName, String key) {
        SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(key);
        SharedPreferencesCompat.apply(editor);
    }

    /**
     * 清除所有数据
     *
     * @param context context
     */
    public static void clear(Context context) {
        clear(context, FILE_NAME);
    }

    /**
     * 清除所有数据
     *
     * @param context context
     * @param spName  spName
     */
    public static void clear(Context context, String spName) {
        SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        SharedPreferencesCompat.apply(editor);
    }

    /**
     * 查询某个key是否已经存在
     *
     * @param context context
     * @param key     key
     * @return boolean
     */
    public static boolean contains(Context context, String key) {
        return contains(context, FILE_NAME, key);
    }

    /**
     * 查询某个key是否已经存在
     *
     * @param context context
     * @param spName  spName
     * @param key     key
     * @return boolean
     */
    public static boolean contains(Context context, String spName, String key) {
        if (null != context) {
            SharedPreferences sp =
                context.getSharedPreferences(spName, Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
            if (null != sp) {
                return sp.contains(key);
            }
        }
        return false;
    }

    /**
     * 返回所有的键值对
     *
     * @param context context
     * @return Map<String, ?>
     */
    public static Map<String, ?> getAll(Context context) {
        return getAll(context, FILE_NAME);
    }

    /**
     * 返回所有的键值对
     *
     * @param context context
     * @param spName  spName
     * @return Map<String, ?>
     */
    public static Map<String, ?> getAll(Context context, String spName) {
        SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
        return sp.getAll();
    }

    /**
     * 创建一个解决SharedPreferencesCompat.apply方法的一个兼容类
     */
    private static class SharedPreferencesCompat {
        private static final Method APPLY_METHOD = findApplyMethod();

        /**
         * 反射查找apply的方法
         */
        @SuppressWarnings({"unchecked", "rawtypes"})
        private static Method findApplyMethod() {
            try {
                Class clz = SharedPreferences.Editor.class;
                return clz.getMethod("apply");
            } catch (NoSuchMethodException e) {
                Log.e(TAG, "NoSuchMethod Exception");
            }

            return null;
        }

        /**
         * 如果找到则使用apply执行，否则使用commit
         *
         * @param editor SharedPreferences.Editor
         */
        public static void apply(SharedPreferences.Editor editor) {
            try {
                if (APPLY_METHOD != null) {
                    APPLY_METHOD.invoke(editor);
                    return;
                }
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "IllegalArgument Exception");
            } catch (IllegalAccessException e) {
                Log.e(TAG, "IllegalAccess Exception");
            } catch (InvocationTargetException e) {
                Log.e(TAG, "InvocationTarget Exception");
            }
            editor.commit();
        }
    }
}