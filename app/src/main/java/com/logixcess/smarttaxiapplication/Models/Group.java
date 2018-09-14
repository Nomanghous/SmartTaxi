package com.logixcess.smarttaxiapplication.Models;

import android.os.Parcel;
import android.os.Parcelable;

public class Group {
    public String group_id, user_id,driver_id;
    private long time;
    private String order_id;
    private String region_name;
    private int radius_constraint;
    private double startingLat, startingLng;
    
    
    
    
    
    public Group(){}

    public Group(String group_id, String user_id, long time,String driver_id) {
        this.group_id = group_id;
        this.user_id = user_id;
        this.time = time;
        this.driver_id = driver_id;
    }

    public int getRadius_constraint() {
        return radius_constraint;
    }

    public void setRadius_constraint(int radius_constraint) {
        this.radius_constraint = radius_constraint;
    }

    public double getStartingLat() {
        return startingLat;
    }

    public void setStartingLat(double startingLat) {
        this.startingLat = startingLat;
    }

    public double getStartingLng() {
        return startingLng;
    }

    public void setStartingLng(double startingLng) {
        this.startingLng = startingLng;
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
