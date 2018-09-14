package com.logixcess.smarttaxiapplication.Models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.List;

public class SharedRide extends Group implements Parcelable {
    private HashMap<String, Boolean> passengers;
    private HashMap<String, Boolean> orderIDs;
    private HashMap<String, UserFareRecord> passengerFares; // passengerID and Fare
    private HashMap<String,List<RoutePoints>> allJourneyPoints;
    
    @Exclude
    private HashMap<String,LatLng> startingPoints;
    
    @Exclude
    private HashMap<String,LatLng> endingPoints;
    
    
    public SharedRide(HashMap<String,Boolean> passengers, HashMap<String, Boolean> orderIds,HashMap<String, UserFareRecord> passengerFares,
                      HashMap<String,List<RoutePoints>> allJourneyPoints){
        this.passengers = passengers;
        this.orderIDs = orderIds;
        this.passengerFares = passengerFares;
        this.allJourneyPoints = allJourneyPoints;
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
    
    public HashMap<String, UserFareRecord> getPassengerFares() {
        return passengerFares;
    }
    
    public void setPassengerFares(HashMap<String, UserFareRecord> passengerFares) {
        this.passengerFares = passengerFares;
    }
    
    public static Creator<SharedRide> getCREATOR() {
        return CREATOR;
    }
    
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }
    
    
    
    
    
    
    
    
    /*
    * Excluded
    *
    * */
    
    @Exclude
    public HashMap<String, LatLng> getStartingPoints() {
        return startingPoints;
    }
    @Exclude
    public void setStartingPoints(HashMap<String, LatLng> startingPoints) {
        this.startingPoints = startingPoints;
    }
    @Exclude
    public HashMap<String, LatLng> getEndingPoints() {
        return endingPoints;
    }
    @Exclude
    public void setEndingPoints(HashMap<String, LatLng> endingPoints) {
        this.endingPoints = endingPoints;
    }
    
    public HashMap<String, List<RoutePoints>> getAllJourneyPoints() {
        return allJourneyPoints;
    }
    
    public void setAllJourneyPoints(HashMap<String, List<RoutePoints>> allJourneyPoints) {
        this.allJourneyPoints = allJourneyPoints;
    }
}
