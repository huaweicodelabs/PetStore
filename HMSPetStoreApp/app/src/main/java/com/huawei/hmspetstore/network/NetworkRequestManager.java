package com.huawei.hmspetstore.network;

import com.huawei.hms.maps.model.LatLng;
import com.huawei.hms.maps.util.LogM;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Response;

/**
 * NetworkRequestManager
 */
public class NetworkRequestManager {
    private static final String TAG = "NetworkRequestManager";

    // Maximum number of retries.
    private static final int MAX_TIMES = 10;

    /**
     * @param latLng1  origin latitude and longitude
     * @param latLng2  destination latitude and longitude
     * @param listener network listener
     */
    public static void getWalkingRoutePlanningResult(final LatLng latLng1, final LatLng latLng2,
                                                     final OnNetworkListener listener) {

        getWalkingRoutePlanningResult(latLng1, latLng2, listener, 0);
    }

    /**
     * @param latLng1    origin latitude and longitude
     * @param latLng2    destination latitude and longitude
     * @param listener   network listener
     * @param count      last number of retries
     */
    private static void getWalkingRoutePlanningResult(final LatLng latLng1, final LatLng latLng2,
                                                      final OnNetworkListener listener, int count) {
        final int curCount = ++count;
        LogM.e(TAG, "current count: " + curCount);
        new Thread(new Runnable() {
            @Override
            public void run() {
                Response response =
                        NetClient.getNetClient().getWalkingRoutePlanningResult(latLng1, latLng2);
                String result = "";
                String returnCode = "";
                String returnDesc = "";

                try {
                    result = response.body().string();
                    JSONObject jsonObject = new JSONObject(result);
                    returnCode = jsonObject.optString("returnCode");
                    returnDesc = jsonObject.optString("returnDesc");

                } catch (NullPointerException | IOException | JSONException e) {
                    returnDesc = "Request Fail!";
                }

                if (returnCode.equals("0")) {
                    if (null != listener) {
                        listener.requestSuccess(result);
                    }
                    return;
                }

                if (curCount >= MAX_TIMES) {
                    if (null != listener) {
                        listener.requestFail(returnDesc);
                    }
                    return;
                }
            }
        }).start();
    }

    /**
     * @param latLng1  origin latitude and longitude
     * @param latLng2  destination latitude and longitude
     * @param listener network listener
     */
    public static void getBicyclingRoutePlanningResult(final LatLng latLng1, final LatLng latLng2,
                                                       final OnNetworkListener listener) {
        getBicyclingRoutePlanningResult(latLng1, latLng2, listener, 0);
    }

    /**
     * @param latLng1    origin latitude and longitude
     * @param latLng2    destination latitude and longitude
     * @param listener   network listener
     * @param count      last number of retries
     */
    private static void getBicyclingRoutePlanningResult(final LatLng latLng1, final LatLng latLng2,
                                                        final OnNetworkListener listener, int count) {

        final int curCount = ++count;
        LogM.e(TAG, "current count: " + curCount);
        new Thread(new Runnable() {
            @Override
            public void run() {
                Response response =
                        NetClient.getNetClient().getBicyclingRoutePlanningResult(latLng1, latLng2);
                if (null != response && null != response.body() && response.isSuccessful()) {
                    try {
                        String result = response.body().string();
                        if (null != listener) {
                            listener.requestSuccess(result);
                        }
                        return;
                    } catch (IOException ignored) {

                    }
                }

                String returnCode = "";
                String returnDesc = "";

                try {
                    String result = response.body().string();
                    JSONObject jsonObject = new JSONObject(result);
                    returnCode = jsonObject.optString("returnCode");
                    returnDesc = jsonObject.optString("returnDesc");
                } catch (NullPointerException | IOException | JSONException e) {
                    returnDesc = "Request Fail!";
                }

                if (curCount >= MAX_TIMES) {
                    if (null != listener) {
                        listener.requestFail(returnDesc);
                    }
                    return;
                }
            }
        }).start();
    }

    /**
     * @param latLng1  origin latitude and longitude
     * @param latLng2  destination latitude and longitude
     * @param listener network listener
     */
    public static void getDrivingRoutePlanningResult(final LatLng latLng1, final LatLng latLng2,
                                                     final OnNetworkListener listener) {
        getDrivingRoutePlanningResult(latLng1, latLng2, listener,0);
    }

    /**
     * @param latLng1    origin latitude and longitude
     * @param latLng2    destination latitude and longitude
     * @param listener   network listener
     * @param count      last number of retries
     */
    private static void getDrivingRoutePlanningResult(final LatLng latLng1, final LatLng latLng2,
                                                      final OnNetworkListener listener, int count) {

        final int curCount = ++count;
        LogM.e(TAG, "current count: " + curCount);
        new Thread(new Runnable() {
            @Override
            public void run() {
                Response response =
                        NetClient.getNetClient().getDrivingRoutePlanningResult(latLng1, latLng2);
                if (null != response && null != response.body() && response.isSuccessful()) {
                    try {
                        String result = response.body().string();
                        if (null != listener) {
                            listener.requestSuccess(result);
                        }
                        return;
                    } catch (IOException ignored) {

                    }
                }

                String returnCode = "";
                String returnDesc = "";

                try {
                    String result = response.body().string();
                    JSONObject jsonObject = new JSONObject(result);
                    returnCode = jsonObject.optString("returnCode");
                    returnDesc = jsonObject.optString("returnDesc");
                } catch (NullPointerException | JSONException | IOException e) {
                    returnDesc = "Request Fail!";
                }

                if (curCount >= MAX_TIMES) {
                    if (null != listener) {
                        listener.requestFail(returnDesc);
                    }
                    return;
                }
            }
        }).start();
    }

    public interface OnNetworkListener {

        void requestSuccess(String result);

        void requestFail(String errorMsg);
    }
}