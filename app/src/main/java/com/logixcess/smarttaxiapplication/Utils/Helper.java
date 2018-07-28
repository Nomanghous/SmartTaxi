package com.logixcess.smarttaxiapplication.Utils;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.logixcess.smarttaxiapplication.Models.Order;

public class Helper {

    public static final String REF_ORDERS = "orders";
    public static final String REF_USERS = "users";
    public static final String REF_GROUPS = "groups";
    public static final String REF_PASSENGERS = "passengers";
    public static final String REF_DRIVERS = "drivers";
    public static final String BROADCAST_DRIVER = "broadcast_drivers";
    public static Order CURRENT_ORDER = null;
    public static String polylinesSeparator = "___and___";
    public static double SELECTED_RADIUS = 10 * 1.602;


    public static boolean checkWithinRadius(Location mine, LatLng other) {
        Location pickup = new Location("me");
        pickup.setLatitude(other.latitude);
        pickup.setLongitude(other.longitude);
        return mine.distanceTo(pickup) < SELECTED_RADIUS;
    }

}
