package com.huawei.hmspetstore.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.huawei.hms.maps.model.LatLng;

public class Place implements Parcelable {
    private String siteId;
    private String name;
    private String formatAddress;
    private double distance;
    public LatLng latLng;

    public Place() {
    }

    protected Place(Parcel in) {
        siteId = in.readString();
        name = in.readString();
        formatAddress = in.readString();
        distance = in.readDouble();
        latLng = in.readParcelable(LatLng.class.getClassLoader());
    }

    public static final Creator<Place> CREATOR = new Creator<Place>() {
        @Override
        public Place createFromParcel(Parcel in) {
            return new Place(in);
        }

        @Override
        public Place[] newArray(int size) {
            return new Place[size];
        }
    };

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFormatAddress() {
        return formatAddress;
    }

    public void setFormatAddress(String formatAddress) {
        this.formatAddress = formatAddress;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(siteId);
        dest.writeString(name);
        dest.writeString(formatAddress);
        dest.writeDouble(distance);
        dest.writeParcelable(latLng, flags);
    }

    @Override
    public String toString() {
        return "Place{" +
                "siteId='" + siteId + '\'' +
                ", name='" + name + '\'' +
                ", formatAddress='" + formatAddress + '\'' +
                ", distance=" + distance +
                ", latLng=" + latLng +
                '}';
    }
}