package com.huawei.hmspetstore.bean;

import android.annotation.SuppressLint;
import android.content.Context;

import com.huawei.hms.iap.entity.InAppPurchaseData;
import com.huawei.hmspetstore.R;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class OrderBean implements Serializable {

    private static final long serialVersionUID = 9150175648504081211L;

    private String time;
    private String status;
    private String name;
    private long price;

    public OrderBean(Context context, InAppPurchaseData inAppPurchaseData) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd");
        this.time = dateFormat.format(new Date(inAppPurchaseData.getPurchaseTime()));
        this.status = getStatusDesc(context, inAppPurchaseData.getPurchaseState());
        this.name = inAppPurchaseData.getProductName();
        this.price = inAppPurchaseData.getPrice();
    }

    private String getStatusDesc(Context context, int status) {
        switch (status) {
            case 0:
                return context.getString(R.string.iap_buy_done);
            case 1:
                return context.getString(R.string.iap_buy_cancel);
            case 2:
                return context.getString(R.string.iap_buy_refund);
            default:
                return "";
        }
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }

}