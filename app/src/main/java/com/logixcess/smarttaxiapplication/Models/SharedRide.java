package com.logixcess.smarttaxiapplication.Models;

import java.util.HashMap;

public class SharedRide {
    private String group_id;
    private String creator_id; // it will the ID of first passenger
    private HashMap<String, Boolean> passengers;
    private HashMap<String, Boolean> orderIDs;


    public SharedRide(String creatorId, String gId, HashMap<String,Boolean> passengers, HashMap<String, Boolean> orderIds){
        this.group_id = gId;
        this.creator_id = creatorId;
        this.passengers = passengers;
        this.orderIDs = orderIds;
    }

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

    public String getCreator_id() {
        return creator_id;
    }

    public void setCreator_id(String creator_id) {
        this.creator_id = creator_id;
    }
}
