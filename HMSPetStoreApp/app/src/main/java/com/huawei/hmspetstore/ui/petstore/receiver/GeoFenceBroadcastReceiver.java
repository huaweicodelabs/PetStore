package com.huawei.hmspetstore.ui.petstore.receiver;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.huawei.hms.location.Geofence;
import com.huawei.hms.location.GeofenceData;
import com.huawei.hmspetstore.R;

import java.util.List;

import static android.content.Context.NOTIFICATION_SERVICE;

public class GeoFenceBroadcastReceiver extends BroadcastReceiver {
    public static final String TAG = "GeoFenceBroadcastReceiver";
    public static final String ACTION_PROCESS_LOCATION = "com.huawei.hmspetstore.ui.petstore.GeoFenceBroadcastReceiver.ACTION_PROCESS_LOCATION";

    @SuppressLint("LongLogTag")
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_PROCESS_LOCATION.equals(action)) {
                GeofenceData geofenceData = GeofenceData.getDataFromIntent(intent);
                if (geofenceData != null) {
                    int errorCode = geofenceData.getErrorCode();
                    int conversion = geofenceData.getConversion();
                    List<Geofence> list = geofenceData.getConvertingGeofenceList();
                    Location mLocation = geofenceData.getConvertingLocation();
                    boolean status = geofenceData.isSuccess();
                    //打印围栏事件信息
                    StringBuilder sb = new StringBuilder();
                    String next = "\n";
                    sb.append("errorcode: " + errorCode + next);
                    sb.append("conversion: " + conversion + next);
                    for (int i = 0; i < list.size(); i++) {
                        sb.append("geoFence id :" + list.get(i).getUniqueId() + next);
                    }
                    sb.append("location is :" + mLocation.getLongitude() + " " + mLocation.getLatitude() + next);
                    sb.append("is successful :" + status);
                    Log.i(TAG, sb.toString());
                    //在通知栏推送信息
                    showNotification(context);
                    Toast.makeText(context, context.getString(R.string.toast_into_geofence) + sb.toString(), Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    /**
     * todo 仅演示进入地理围栏通知
     */
    private void showNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel("geoFence", context.getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(notificationChannel);
            builder = new NotificationCompat.Builder(context, "geoFence");
        } else {
            builder = new NotificationCompat.Builder(context);
        }
        builder.setTicker(context.getString(R.string.notify_ticker));
        builder.setContentTitle(context.getString(R.string.notify_content_title));
        builder.setContentText(context.getString(R.string.notify_content_text));
        builder.setSubText("SubText");
        builder.setContentInfo("ContentInfo");
        builder.setNumber(2);
        builder.setAutoCancel(true);
        builder.setWhen(System.currentTimeMillis());
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setDefaults(NotificationCompat.DEFAULT_ALL);
        Notification notification = builder.build();
        notificationManager.notify(1, notification);
    }
}