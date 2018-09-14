package com.logixcess.smarttaxiapplication.Utils;

import android.location.Location;
import android.net.Uri;

import com.google.android.gms.maps.model.LatLng;

public class Constants
{
    public static String PREFERENCES_KEY = "";
    public static String Database_Path = "https://smarttaxi-c57c4.firebaseio.com/";
    public static String USER_TOKEN = "";
    public static Uri FilePathUri ;
    public static Uri FilePathUri2 ;
    public static String Storage_Path = "images" ;
    public static String region_name = "" ;
    public static float SELECTED_RADIUS = 10000.0f ;//distance in meters
    public static long date_selected_expiry;
    public static long date_selected_issue;
    public static double BASE_FAIR_PER_KM = 50;
    public static String selected_vehicle = Helper.VEHICLE_CAR;
    public static String user_image_path = "";
    public static String group_id = "";
    public static String notificationPayload = "";
    public static int group_radius = 0;

}
