package com.logixcess.smarttaxiapplication.Utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

public class Helper {
    public static final int NOTI_TYPE_ORDER_CREATED = 101;
    public static final int NOTI_TYPE_ORDER_ACCEPTED = 102;
    public static final int NOTI_TYPE_ORDER_COMPLETED = 103;
    public static final int NOTI_TYPE_ORDER_WAITING = 104;
    public static final int NOTI_TYPE_ORDER_WAITING_LONG = 105;
    public static final int NOTI_TYPE_ORDER_CREATED_FOR_SHARED_RIDE = 106;
    public static final int NOTI_TYPE_ACCEPTANCE_FOR_SHARED_RIDE = 107;
    public static final String REF_ORDERS = "Order";
    public static final String REF_USERS = "User";
    public static final String REF_GROUPS = "Group";
    public static final String REF_PASSENGERS = "Passenger";
    public static final String REF_DRIVERS = "Driver";
    public static final String REF_SINGLE_ORDER = "SingleOrder";
    public static final String REF_GROUP_ORDER = "GroupOrder";
    public static final String REF_WAITING_TIME = "UserWaitingTime";
    public static final String REF_ORDER_TO_DRIVER = "OrderToDriver";
    public static final String REF_REQUESTS = "requests";
    public static final String BROADCAST_DRIVER = "broadcast_drivers";
    public static final String BROADCAST_DRIVER_RESPONSE = "BROADCAST_DRIVER_RESPONSE";
    public static final String REF_NOTIFICATIONS = "notifications";
    public static final String VEHICLE_CAR = "car";
    public static final String VEHICLE_MINI = "mini";
    public static final String VEHICLE_NANO = "nano";
    public static final String VEHICLE_VIP = "vip";
    public static final String VEHICLE_THREE_WHEELER = "three_wheeler";
    public static final String REF_FEEBACK = "Feedback";

    public static final String commaSeparator = "-";
    public static final String dotSeparator = "_";
    public static final String BROADCAST_LOCATION = "location";
    public static final int NOTI_TYPE_CALL = 21323;
    
    public static String polylinesSeparator = "___and___";
    public static double SELECTED_RADIUS = 10000; // it's in meters
    public static boolean IS_FROM_CHILD = false;
    public static boolean isAcceptingInvites = false;
    public static List<LatLng> invitationLatlngs;
    
    
    public static String getRefinedLatLngKeyForHashMap(String latlng){
        return latlng.replace("(","").replace(")","").replace(",",commaSeparator).replace(".",dotSeparator);
    }
    
    public static String decodeLatLngKeyForHashMap(String latlng){
        return latlng.replace(commaSeparator, ",").replace(dotSeparator,".");
    }
    
    public static boolean checkWithinRadius(Location mine, LatLng other) {
        if(mine == null)
            return false;
        Location pickup = new Location("me");
        pickup.setLatitude(other.latitude);
        pickup.setLongitude(other.longitude);
        return mine.distanceTo(pickup) < SELECTED_RADIUS;
    }
    public static String getConcatenatedID(String myUid,String uid){
        myUid = myUid.replace("\"","");
        uid = uid.replace("\"","");
        String[] alphaNumericStringArray = new String[]{myUid, uid};
        Arrays.sort(alphaNumericStringArray, new AlphanumericSorting());
        return alphaNumericStringArray[0].concat(alphaNumericStringArray[1]);
    }
    public static Bitmap convertToBitmap(Drawable drawable, int widthPixels, int heightPixels) {
        Bitmap mutableBitmap = Bitmap.createBitmap(widthPixels, heightPixels, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mutableBitmap);
        drawable.setBounds(0, 0, widthPixels, heightPixels);
        drawable.draw(canvas);

        return mutableBitmap;
    }


    public static double roundOffDouble(double value){
        String val = String.valueOf(value);
//        if(val.length() > 8)
//            return Double.valueOf(val.substring(0,10));
//        else
//            return value;
        return value;
    }

}
