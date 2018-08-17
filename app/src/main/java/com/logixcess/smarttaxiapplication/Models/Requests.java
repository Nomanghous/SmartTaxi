package com.logixcess.smarttaxiapplication.Models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;

public class Requests implements Parcelable{
    @Exclude
    public static int STATUS_ACCEPTED = 1, STATUS_PENDING = 0, STATUS_REJECTED = 2;
    String driverId, userId;
    int status;

    public Requests() {

    }

    public Requests(String driverId, String userId, int  status) {
        this.driverId = driverId;
        this.userId = userId;
        this.status = status;
    }

    protected Requests(Parcel in) {
        driverId = in.readString();
        userId = in.readString();
        status = in.readInt();
    }

    public static final Creator<Requests> CREATOR = new Creator<Requests>() {
        @Override
        public Requests createFromParcel(Parcel in) {
            return new Requests(in);
        }

        @Override
        public Requests[] newArray(int size) {
            return new Requests[size];
        }
    };

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(driverId);
        dest.writeString(userId);
        dest.writeInt(status);
    }
}
