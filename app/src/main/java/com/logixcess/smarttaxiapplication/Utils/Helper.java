package com.logixcess.smarttaxiapplication.Utils;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.logixcess.smarttaxiapplication.Models.Order;

public class Helper {

    public static final String REF_ORDERS = "Order";
    public static final String REF_USERS = "User";
    public static final String REF_GROUPS = "Group";
    public static final String REF_PASSENGERS = "Passenger";
    public static final String REF_DRIVERS = "Driver";
    public static final String BROADCAST_DRIVER = "broadcast_drivers";
    public static Order CURRENT_ORDER = null;
    public static String polylinesSeparator = "___and___";
    public static double SELECTED_RADIUS = 10 * 1.602;


    public static boolean checkWithinRadius(Location mine, LatLng other) {
        if(mine == null)
            return false;
        Location pickup = new Location("me");
        pickup.setLatitude(other.latitude);
        pickup.setLongitude(other.longitude);
        return mine.distanceTo(pickup) < SELECTED_RADIUS;
    }

}
