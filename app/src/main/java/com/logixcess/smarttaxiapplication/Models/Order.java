package com.logixcess.smarttaxiapplication.Models;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.design.internal.ParcelableSparseArray;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.List;

public class Order extends ParcelableSparseArray implements Parcelable {

    @Exclude
    public static final int OrderStatusCompleted = 1, OrderStatusCompletedReview = 5,
            OrderStatusInProgress = 2, OrderStatusPending = 3,
                    OrderStatusCancelled = 4, OrderStatusWaiting = 6;
    
    
    @Exclude
    private boolean[] NotificaionsDone = new boolean[4];
    
    private String pickup, dropoff,
            user_id, user_name, scheduled_time, driver_id, driver_name, vehicle_id,
            total_kms, waiting_time, pickup_time,pickup_date, estimated_cost, group_id;
    
    private double total_fare;
    
//    UserFareRecord fareRecord;
//    private List<RoutePoints> journeyPoints;
    private String order_id;
    private Boolean isScheduled, isShared, isOnRide;
    private int status = 0; // nothing
    private Double pickupLat, pickupLong, dropoffLat, dropoffLong;
    private ArrayList<RoutePoints> SelectedRoute;
    private int passenger_status;
    
    public Order() {
    
    }

    public Order(String pickup, String dropoff, Double pickupLat, Double pickupLong, Double dropoffLat, Double dropoffLong, String user_id, String user_name, String scheduled_time, String driver_id, String driver_name, String vehicle_id, String total_kms, String waiting_time, String pickup_time, String pickup_date, String estimated_cost, Boolean isScheduled,
                 Boolean isShared, int status, ArrayList<RoutePoints> selectedRoute,int passenger_status,boolean isOnRide) {
        this.pickup = pickup;
        this.dropoff = dropoff;
        this.pickupLat = pickupLat;
        this.pickupLong = pickupLong;
        this.dropoffLat = dropoffLat;
        this.dropoffLong = dropoffLong;
        this.user_id = user_id;
        this.user_name = user_name;
        this.scheduled_time = scheduled_time;
        this.driver_id = driver_id;
        this.driver_name = driver_name;
        this.vehicle_id = vehicle_id;
        this.total_kms = total_kms;
        this.waiting_time = waiting_time;
        this.pickup_time = pickup_time;
        this.pickup_date = pickup_date;
        this.estimated_cost = estimated_cost;
        this.isScheduled = isScheduled;
        this.isShared = isShared;
        this.status = status;
        this.SelectedRoute = selectedRoute;
        this.passenger_status = passenger_status;
        this.isOnRide = isOnRide;
    }
    
    public String getGroup_id() {
        return group_id;
    }
    
    public void setGroup_id(String group_id) {
        this.group_id = group_id;
    }
    
    public static final Creator<Order> CREATOR = new Creator<Order>() {
        @Override
        public Order createFromParcel(Parcel in) {
            return new Order(in);
        }

        @Override
        public Order[] newArray(int size) {
            return new Order[size];
        }
    };
    
    @Exclude
    public boolean[] getNotificaionsDone() {
        return NotificaionsDone;
    }
    @Exclude
    public void setNotificaionsDone(boolean[] notificaionsDone) {
        NotificaionsDone = notificaionsDone;
    }
    
    public double getTotal_fare() {
            return Math.round(total_fare);
    }
    
    public void setTotal_fare(double total_fare) {
        this.total_fare = total_fare;
    }
    
    public String getEstimated_cost() {
        return String.valueOf(Math.round(Double.valueOf(estimated_cost)));
    }

    public void setEstimated_cost(String estimated_cost) {
        this.estimated_cost = estimated_cost;
    }

    public String getPickup() {
        return pickup;
    }

    public void setPickup(String pickup) {
        this.pickup = pickup;
    }

    public String getDropoff() {
        return dropoff;
    }

    public void setDropoff(String dropoff) {
        this.dropoff = dropoff;
    }

    public Double getPickupLat() {
        return pickupLat;
    }
    
    public void setPickupLat(Double pickupLat) {
        this.pickupLat = pickupLat;
    }

    public Double getPickupLong() {
        return pickupLong;
    }

    public void setPickupLong(Double pickupLong) {
        this.pickupLong = pickupLong;
    }

    public Double getDropoffLat() {
        return dropoffLat;
    }

    public void setDropoffLat(Double dropoffLat) {
        this.dropoffLat = dropoffLat;
    }

    public Double getDropoffLong() {
        return dropoffLong;
    }

    public void setDropoffLong(Double dropoffLong) {
        this.dropoffLong = dropoffLong;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getScheduled_time() {
        return scheduled_time;
    }

    public void setScheduled_time(String scheduled_time) {
        this.scheduled_time = scheduled_time;
    }

    public String getDriver_id() {
        return driver_id;
    }

    public void setDriver_id(String driver_id) {
        this.driver_id = driver_id;
    }

    public String getDriver_name() {
        return driver_name;
    }

    public void setDriver_name(String driver_name) {
        this.driver_name = driver_name;
    }

    public String getPickup_date() {
        return pickup_date;
    }

    public void setPickup_date(String pickup_date) {
        this.pickup_date = pickup_date;
    }

    public String getPickup_time() {
        return pickup_time;
    }

    public void setPickup_time(String pickup_time) {
        this.pickup_time = pickup_time;
    }

    public String getVehicle_id() {
        return vehicle_id;
    }

    public void setVehicle_id(String vehicle_id) {
        this.vehicle_id = vehicle_id;
    }

    public String getTotal_kms() {
        return total_kms;
    }

    public void setTotal_kms(String total_kms) {
        this.total_kms = total_kms;
    }

    public String getWaiting_time() {
        return waiting_time;
    }

    public void setWaiting_time(String waiting_time) {
        this.waiting_time = waiting_time;
    }

    public Boolean getShared() {
        return isShared;
    }

    public void setShared(Boolean shared) {
        isShared = shared;
    }

    public Boolean getScheduled() {
        return isScheduled;
    }
    
    public Boolean getOnRide() {
        return isOnRide;
    }
    
    public void setOnRide(Boolean onRide) {
        isOnRide = onRide;
    }
    
    public void setScheduled(Boolean scheduled) {
        isScheduled = scheduled;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getOrder_id() {
        return order_id;
    }

    public void setOrder_id(String order_id) {
        this.order_id = order_id;
    }

    @Override
    public int describeContents() {
        return 0;
    }
    protected Order(Parcel in) {
        pickup = in.readString();
        dropoff = in.readString();
        pickupLat = in.readDouble();
        pickupLong = in.readDouble();
        dropoffLat = in.readDouble();
        dropoffLong = in.readDouble();
        user_id = in.readString();
        user_name = in.readString();
        scheduled_time = in.readString();
        driver_id = in.readString();
        driver_name = in.readString();
        vehicle_id = in.readString();
        total_kms = in.readString();
        waiting_time = in.readString();
        pickup_time = in.readString();
        pickup_date = in.readString();
        order_id = in.readString();
        estimated_cost = in.readString();
        byte tmpIsScheduled = in.readByte();
        isScheduled = tmpIsScheduled == 0 ? null : tmpIsScheduled == 1;
        byte tmpIsShared = in.readByte();
        isShared = tmpIsShared == 0 ? null : tmpIsShared == 1;
        status = in.readInt();
        passenger_status = in.readInt();
        SelectedRoute = in.readArrayList(LatLng.class.getClassLoader());
        byte tmIsOnRide = in.readByte();
        isOnRide = tmIsOnRide == 0 ? null : tmIsOnRide == 1;
        total_fare = in.readDouble();
//        journeyPoints = in.readArrayList(LatLng.class.getClassLoader());
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(pickup);
        dest.writeString(dropoff);
        dest.writeDouble(pickupLat);
        dest.writeDouble(pickupLong);
        dest.writeDouble(dropoffLat);
        dest.writeDouble(dropoffLong);
        dest.writeString(user_id);
        dest.writeString(user_name);
        dest.writeString(scheduled_time);
        dest.writeString(driver_id);
        dest.writeString(driver_name);
        dest.writeString(vehicle_id);
        dest.writeString(total_kms);
        dest.writeString(waiting_time);
        dest.writeString(pickup_time);
        dest.writeString(pickup_date);
        dest.writeString(order_id);
        dest.writeString(estimated_cost);
        dest.writeByte((byte) (isScheduled == null ? 0 : isScheduled ? 1 : 2));
        dest.writeByte((byte) (isShared == null ? 0 : isShared ? 1 : 2));
        dest.writeInt(status);
        dest.writeInt(passenger_status);
        dest.writeList(SelectedRoute);
        dest.writeByte((byte) (isOnRide == null ? 0 : isOnRide ? 1 : 2));
        dest.writeDouble(total_fare);
//        dest.writeList(journeyPoints);
    }

    public ArrayList<RoutePoints> getSelectedRoute() {
        return SelectedRoute;
    }

    public void setSelectedRoute(ArrayList<RoutePoints> selectedRoute) {
        this.SelectedRoute = selectedRoute;
    }

    public int getPassenger_status() {
        return passenger_status;
    }

    public void setPassenger_status(int passenger_status) {
        this.passenger_status = passenger_status;
    }
//
//    public UserFareRecord getFareRecord() {
//        return fareRecord;
//    }
//
//    public void setFareRecord(UserFareRecord fareRecord) {
//        this.fareRecord = fareRecord;
//    }
//
//    public List<RoutePoints> getJourneyPoints() {
//        return journeyPoints;
//    }
//
//    public void setJourneyPoints(List<RoutePoints> journeyPoints) {
//        this.journeyPoints = journeyPoints;
//    }
}
