package com.logixcess.smarttaxiapplication.Utils;

import android.location.Location;
import android.net.Uri;

import com.google.android.gms.maps.model.LatLng;

public class Constants
{
    public static String PREFERENCES_KEY = "";
    public static String Database_Path = "https://smarttaxi-c57c4.firebaseio.com/";
    public static String USER_TOKEN = "";
    public static String USER_ID = "";
    public static Uri FilePathUri ;
    public static Uri FilePathUri2 ;
    public static String Storage_Path = "images" ;
    public static String region_name = "" ;
    public static float SELECTED_RADIUS = 10000.0f ;//distance in meters
    public static long date_selected_expiry;
    public static long date_selected_issue;
    public static final int PRIMARY_USER = 1;
    public static final int SECONDARY_USER = 2;
    public static final int TERTIARY_USER = 3;
    public static double BASE_FAIR_PER_KM = 40;
    public static String user_image_path = "";
    public static String group_id = "";
}
