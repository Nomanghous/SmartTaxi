package com.logixcess.smarttaxiapplication.Models;

import android.os.Parcel;
import android.os.Parcelable;

public class RoutePoints implements Parcelable {

    private double latitude, longitude;
    public RoutePoints(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public RoutePoints(){

    }

    protected RoutePoints(Parcel in) {
        latitude = in.readDouble();
        longitude = in.readDouble();
    }

    public static final Creator<RoutePoints> CREATOR = new Creator<RoutePoints>() {
        @Override
        public RoutePoints createFromParcel(Parcel in) {
            return new RoutePoints(in);
        }

        @Override
        public RoutePoints[] newArray(int size) {
            return new RoutePoints[size];
        }
    };

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
    }
}
