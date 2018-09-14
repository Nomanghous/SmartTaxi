/*
 * Copyright (C) Logixcess, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Noman Ghous <Nomanghous@hotmail.com>, Copyright (c) 2018.
 *
 */

package com.logixcess.smarttaxiapplication.Models;


import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;
import java.util.List;

public class UserFareRecord {
    String userId;
    List<LatLng> latLngs;
    HashMap<LatLng, Double> userFare;
    double baseFare;
    public HashMap<LatLng, Double> getUserFare() {
        return userFare;
    }
    
    public void setUserFare(HashMap<LatLng, Double> userFare) {
        this.userFare = userFare;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public List<LatLng> getLatLngs() {
        return latLngs;
    }
    
    public void setLatLngs(List<LatLng> latLngs) {
        this.latLngs = latLngs;
    }
    
    public double getBaseFare() {
        return baseFare;
    }
    
    public void setBaseFare(double baseFare) {
        this.baseFare = baseFare;
    }
}
