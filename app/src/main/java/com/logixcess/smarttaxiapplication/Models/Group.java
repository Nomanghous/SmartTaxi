package com.logixcess.smarttaxiapplication.Models;

import android.os.Parcel;
import android.os.Parcelable;

public class Group {
    String group_id, user_id,driver_id;
    long time;
    String order_id;
    String region_name;

    public Group(){}

    public Group(String group_id, String user_id, long time,String driver_id) {
        this.group_id = group_id;
        this.user_id = user_id;
        this.time = time;
        this.driver_id = driver_id;
    }

    protected Group(Parcel in) {
        group_id = in.readString();
        user_id = in.readString();
        time = in.readLong();
        order_id =in.readString();
        region_name =in.readString();
        driver_id =in.readString();
    }

    public String getGroup_id() {
        return group_id;
    }

    public void setGroup_id(String group_id) {
        this.group_id = group_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getOrder_id() {
        return order_id;
    }

    public void setOrder_id(String order_id) {
        this.order_id = order_id;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }


    public String getRegion_name() {
        return region_name;
    }

    public void setRegion_name(String region_name) {
        this.region_name = region_name;
    }

    public String getDriver_id() {
        return driver_id;
    }

    public void setDriver_id(String driver_id) {
        this.driver_id = driver_id;
    }
}
