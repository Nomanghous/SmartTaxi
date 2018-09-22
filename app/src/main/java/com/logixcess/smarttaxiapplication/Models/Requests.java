package com.logixcess.smarttaxiapplication.Models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;

public class Requests implements Parcelable{
    @Exclude
    public static int STATUS_ACCEPTED = 1, STATUS_PENDING = 0, STATUS_REJECTED = 2;
    boolean forSharedRide;
    String receiverId, senderId, driverId, group_id, order_id, vehicle_type;
    int status;
    
    
    public Requests() {

    }

    public Requests(String driverId, String userId, int  status, boolean sharedRide) {
        this.receiverId = driverId;
        this.senderId = userId;
        this.status = status;
        this.forSharedRide = sharedRide;
    }

    protected Requests(Parcel in) {
        receiverId = in.readString();
        senderId = in.readString();
        status = in.readInt();
        
    }
    
    public String getVehicle_type() {
        return vehicle_type;
    }
    
    public void setVehicle_type(String vehicle_type) {
        this.vehicle_type = vehicle_type;
    }
    
    public String getGroup_id() {
        return group_id;
    }
    
    public void setGroup_id(String group_id) {
        this.group_id = group_id;
    }
    
    public String getOrder_id() {
        return order_id;
    }
    
    public void setOrder_id(String order_id) {
        this.order_id = order_id;
    }
    
    public String getDriverId() {
        return driverId;
    }
    
    public void setDriverId(String driverId) {
        this.driverId = driverId;
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
    
    public Requests(String driverId, String userId, int status) {
        this.receiverId = driverId;
        this.senderId = userId;
        this.status = status;
        this.forSharedRide = false;
    
    }
    
    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
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
        dest.writeString(receiverId);
        dest.writeString(senderId);
        dest.writeInt(status);
    }
}
