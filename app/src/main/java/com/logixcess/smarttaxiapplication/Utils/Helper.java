package com.logixcess.smarttaxiapplication.Utils;

import android.location.Location;
import com.google.android.gms.maps.model.LatLng;
import com.logixcess.smarttaxiapplication.Models.Order;

import java.util.Arrays;

public class Helper {
    public static final int NOTI_TYPE_ORDER_CREATED = 101;
    public static final int NOTI_TYPE_ORDER_ACCEPTED = 102;
    public static final int NOTI_TYPE_ORDER_COMPLETED = 103;
    public static final int NOTI_TYPE_ORDER_WAITING = 104;
    public static final int NOTI_TYPE_ORDER_WAITING_LONG = 105;
    public static final String REF_ORDERS = "Order";
    public static final String REF_USERS = "User";
    public static final String REF_GROUPS = "Group";
    public static final String REF_PASSENGERS = "Passenger";
    public static final String REF_DRIVERS = "Driver";
    public static final String REF_SINGLE_ORDER = "SingleOrder";
    public static final String REF_GROUP_ORDER = "GroupOrder";
    public static final String REF_ORDER_TO_DRIVER = "OrderToDriver";
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
    public static String getConcatenatedID(String myUid,String uid){
        String[] alphaNumericStringArray = new String[]{myUid, uid};
        Arrays.sort(alphaNumericStringArray, new AlphanumericSorting());
        return alphaNumericStringArray[0] + alphaNumericStringArray[1];
    }

}
