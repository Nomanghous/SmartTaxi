package com.logixcess.smarttaxiapplication.Models;

import android.os.Parcel;
import android.os.Parcelable;

public class Group implements Parcelable{
    String group_id, trip_id, user_id, distanceKM, cost_id, pickup_lat, pickup_long, dropoff_lat, dropoff_long;
    long time;

    protected Group(Parcel in) {
        group_id = in.readString();
        trip_id = in.readString();
        user_id = in.readString();
        distanceKM = in.readString();
        cost_id = in.readString();
        pickup_lat = in.readString();
        pickup_long = in.readString();
        dropoff_lat = in.readString();
        dropoff_long = in.readString();
        time = in.readLong();
    }

    public static final Creator<Group> CREATOR = new Creator<Group>() {
        @Override
        public Group createFromParcel(Parcel in) {
            return new Group(in);
        }

        @Override
        public Group[] newArray(int size) {
            return new Group[size];
        }
    };

    public String getGroup_id() {
        return group_id;
    }

    public void setGroup_id(String group_id) {
        this.group_id = group_id;
    }

    public String getTrip_id() {
        return trip_id;
    }

    public void setTrip_id(String trip_id) {
        this.trip_id = trip_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public static Creator<Group> getCREATOR() {
        return CREATOR;
    }

    public String getDistanceKM() {
        return distanceKM;
    }

    public void setDistanceKM(String distanceKM) {
        this.distanceKM = distanceKM;
    }

    public String getCost_id() {
        return cost_id;
    }

    public void setCost_id(String cost_id) {
        this.cost_id = cost_id;
    }

    public String getPickup_lat() {
        return pickup_lat;
    }

    public void setPickup_lat(String pickup_lat) {
        this.pickup_lat = pickup_lat;
    }

    public String getPickup_long() {
        return pickup_long;
    }

    public void setPickup_long(String pickup_long) {
        this.pickup_long = pickup_long;
    }

    public String getDropoff_lat() {
        return dropoff_lat;
    }

    public void setDropoff_lat(String dropoff_lat) {
        this.dropoff_lat = dropoff_lat;
    }

    public String getDropoff_long() {
        return dropoff_long;
    }

    public void setDropoff_long(String dropoff_long) {
        this.dropoff_long = dropoff_long;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(group_id);
        dest.writeString(trip_id);
        dest.writeString(user_id);
        dest.writeString(distanceKM);
        dest.writeString(cost_id);
        dest.writeString(pickup_lat);
        dest.writeString(pickup_long);
        dest.writeString(dropoff_lat);
        dest.writeString(dropoff_long);
        dest.writeLong(time);
    }
}
