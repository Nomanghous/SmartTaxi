package com.logixcess.smarttaxiapplication.Models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;

public class SharedRide extends Group implements Parcelable {
    private HashMap<String, Boolean> passengers;
    private HashMap<String, Boolean> orderIDs;


    public SharedRide(HashMap<String,Boolean> passengers, HashMap<String, Boolean> orderIds){
        this.passengers = passengers;
        this.orderIDs = orderIds;
    }

    public SharedRide() {

    }

    protected SharedRide(Parcel in) {

    }

    public static final Creator<SharedRide> CREATOR = new Creator<SharedRide>() {
        @Override
        public SharedRide createFromParcel(Parcel in) {
            return new SharedRide(in);
        }

        @Override
        public SharedRide[] newArray(int size) {
            return new SharedRide[size];
        }
    };

    public String getGroup_id() {
        return group_id;
    }

    public void setGroup_id(String group_id) {
        this.group_id = group_id;
    }

    public HashMap<String, Boolean> getPassengers() {
        return passengers;
    }

    public void setPassengers(HashMap<String, Boolean> passengers) {
        this.passengers = passengers;
    }

    public HashMap<String, Boolean> getOrderIDs() {
        return orderIDs;
    }

    public void setOrderIDs(HashMap<String, Boolean> orderIDs) {
        this.orderIDs = orderIDs;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }
}
