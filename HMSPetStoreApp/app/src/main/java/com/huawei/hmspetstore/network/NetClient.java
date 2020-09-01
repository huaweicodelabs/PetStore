
package com.huawei.hmspetstore.network;

import android.content.Context;

import com.huawei.hms.maps.model.LatLng;
import com.huawei.hmspetstore.R;
import com.huawei.hmspetstore.util.SystemUtil;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * NetClient
 */
public class NetClient {

    private static NetClient netClient;

    private static OkHttpClient client;

    private String mDefaultKey;
    private String mWalkingRoutePlanningURL;
    private String mBicyclingRoutePlanningURL;
    private String mDrivingRoutePlanningURL;

    private static MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private NetClient() {
        client = initOkHttpClient();
    }

    public OkHttpClient initOkHttpClient() {
        if (client == null) {
            client = new OkHttpClient.Builder().readTimeout(10000, TimeUnit.MILLISECONDS)// Set the read timeout.
                    .connectTimeout(10000, TimeUnit.MILLISECONDS)// Set the connect timeout.
                    .build();
        }
        return client;
    }

    public static NetClient getNetClient() {
        if (netClient == null) {
            netClient = new NetClient();
        }
        return netClient;
    }

    public void init(@NotNull Context context) {
        this.mDefaultKey = SystemUtil.getApiKey(context);
        this.mWalkingRoutePlanningURL = context.getString(R.string.walking_route_planning_url);
        this.mBicyclingRoutePlanningURL = context.getString(R.string.bicycling_route_planning_url);
        this.mDrivingRoutePlanningURL = context.getString(R.string.driving_route_planning_url);
    }

    /**
     * @param latLng1    origin latitude and longitude
     * @param latLng2    destination latitude and longitude
     * @return Response
     */
    public Response getWalkingRoutePlanningResult(LatLng latLng1, LatLng latLng2) {
        String key = mDefaultKey;
        String url = mWalkingRoutePlanningURL + "?key=" + key;

        Response response = null;
        JSONObject origin = new JSONObject();
        JSONObject destination = new JSONObject();
        JSONObject json = new JSONObject();
        try {
            origin.put("lat", latLng1.latitude);
            origin.put("lng", latLng1.longitude);

            destination.put("lat", latLng2.latitude);
            destination.put("lng", latLng2.longitude);

            json.put("origin", origin);
            json.put("destination", destination);

            RequestBody requestBody = RequestBody.create(JSON, String.valueOf(json));
            Request request = new Request.Builder().url(url).post(requestBody).build();
            response = getNetClient().initOkHttpClient().newCall(request).execute();
        } catch (JSONException | IOException ignored) {

        }
        return response;
    }

    /**
     * @param latLng1    origin latitude and longitude
     * @param latLng2    destination latitude and longitude
     * @return Response
     */
    public Response getBicyclingRoutePlanningResult(LatLng latLng1, LatLng latLng2) {
        String key = mDefaultKey;
        String url = mBicyclingRoutePlanningURL + "?key=" + key;

        Response response = null;
        JSONObject origin = new JSONObject();
        JSONObject destination = new JSONObject();
        JSONObject json = new JSONObject();
        try {
            origin.put("lat", latLng1.latitude);
            origin.put("lng", latLng1.longitude);

            destination.put("lat", latLng2.latitude);
            destination.put("lng", latLng2.longitude);

            json.put("origin", origin);
            json.put("destination", destination);

            RequestBody requestBody = RequestBody.create(JSON, String.valueOf(json));
            Request request = new Request.Builder().url(url).post(requestBody).build();
            response = getNetClient().initOkHttpClient().newCall(request).execute();
        } catch (JSONException | IOException ignored) {

        }
        return response;
    }

    /**
     * @param latLng1    origin latitude and longitude
     * @param latLng2    destination latitude and longitude
     * @return Response
     */
    public Response getDrivingRoutePlanningResult(LatLng latLng1, LatLng latLng2) {
        String key = mDefaultKey;
        String url = mDrivingRoutePlanningURL + "?key=" + key;

        Response response = null;
        JSONObject origin = new JSONObject();
        JSONObject destination = new JSONObject();
        JSONObject json = new JSONObject();
        try {
            origin.put("lat", latLng1.latitude);
            origin.put("lng", latLng1.longitude);

            destination.put("lat", latLng2.latitude);
            destination.put("lng", latLng2.longitude);

            json.put("origin", origin);
            json.put("destination", destination);

            RequestBody requestBody = RequestBody.create(JSON, String.valueOf(json));
            Request request = new Request.Builder().url(url).post(requestBody).build();
            response = getNetClient().initOkHttpClient().newCall(request).execute();
        } catch (JSONException | IOException ignored) {

        }
        return response;
    }
}