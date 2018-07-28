package com.logixcess.smarttaxiapplication.Utils;

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
    public static float SELECTED_RADIUS = 10000.0f ;//distance in meters
    public static LatLng USER_CURRENT_LOCATION = new LatLng(0,0) ;//distance in meters
    public static long date_selected_expiry;
    public static long date_selected_issue;
}
