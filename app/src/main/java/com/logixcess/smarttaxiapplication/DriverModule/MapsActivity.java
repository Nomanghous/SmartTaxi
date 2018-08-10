package com.logixcess.smarttaxiapplication.DriverModule;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.logixcess.smarttaxiapplication.Models.NotificationPayload;
import com.logixcess.smarttaxiapplication.Models.Order;
import com.logixcess.smarttaxiapplication.Models.Passenger;
import com.logixcess.smarttaxiapplication.Models.RoutePoints;
import com.logixcess.smarttaxiapplication.Models.SharedRide;
import com.logixcess.smarttaxiapplication.Models.User;
import com.logixcess.smarttaxiapplication.R;
import com.logixcess.smarttaxiapplication.Services.LocationManagerService;
import com.logixcess.smarttaxiapplication.Utils.Helper;
import com.logixcess.smarttaxiapplication.Utils.PushNotifictionHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, RoutingListener {


    boolean[] NotificaionsDone = new boolean[4];

    public static final String KEY_CURRENT_SHARED_RIDE = "key_shared_ride";
    private GoogleMap mMap;
    private Order CURRENT_ORDER = null;
    private boolean IS_RIDE_SHARED = false;
    private FirebaseDatabase firebase_db;
    private DatabaseReference db_ref_order;
    private DatabaseReference db_ref_user;
    private DatabaseReference db_ref_group;
    private FirebaseUser USER_ME;
    private Location myLocation = null;
    private LatLng dropoff, pickup;
    private String CURRENT_ORDER_ID = "";
    private LatLng start, end;
    private ArrayList<LatLng> waypoints;
    public static final String KEY_CURRENT_ORDER = "current_order";
    private Marker mDriverMarker;
    private ArrayList<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.colorPrimary, R.color.colorPrimary,R.color.colorPrimaryDark,R.color.colorAccent,R.color.primary_dark_material_light};

    private double totalDistance = 100, totalTime = 120; // total time in minutes
    private double distanceRemaining = 90;
    private DatabaseReference db_ref, db_ref_driver;
    private String selectedPassengerId;
    private User SELECTED_PASSENGER;
    private LatLng driver = null;
    private SharedRide CURRENT_SHARED_RIDE;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        firebase_db = FirebaseDatabase.getInstance();
        db_ref_order = firebase_db.getReference().child(Helper.REF_ORDERS);
        db_ref_user = firebase_db.getReference().child(Helper.REF_USERS);
        db_ref_group = firebase_db.getReference().child(Helper.REF_GROUPS);
        USER_ME = FirebaseAuth.getInstance().getCurrentUser();
        NotificaionsDone[0] = false;
        NotificaionsDone[1] = false;
        NotificaionsDone[2] = false;
        NotificaionsDone[3] = false;
        Bundle bundle = getIntent().getExtras();
        if(bundle != null && bundle.containsKey(KEY_CURRENT_ORDER)){
            CURRENT_ORDER = bundle.getParcelable(KEY_CURRENT_ORDER);
            if(CURRENT_ORDER != null && CURRENT_ORDER.getShared()){
                if(bundle.containsKey(KEY_CURRENT_SHARED_RIDE)) {
                    CURRENT_SHARED_RIDE = bundle.getParcelable(KEY_CURRENT_SHARED_RIDE);
                    IS_RIDE_SHARED = true;
                }
            }else if(CURRENT_ORDER != null){
                IS_RIDE_SHARED = false;
            }else {
                finish();
                return;
            }

            askLocationPermission();
            setContentView(R.layout.activity_maps);
            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
            db_ref = FirebaseDatabase.getInstance().getReference();
            db_ref_driver = db_ref.child(Helper.REF_USERS).child(CURRENT_ORDER.getUser_id());
            db_ref_driver.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        SELECTED_PASSENGER = dataSnapshot.getValue(User.class);
                        if(SELECTED_PASSENGER != null && mMap != null) {
                            requestNewRoute();
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            new Timer().schedule(new Every10Seconds(),5000,10000);
        }else{
            // driver id not provided
            finish();
        }
    }

    private void requestNewRoute() {
        if(myLocation == null)
            return;
        driver = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
        if(pickup == null)
            pickup = new LatLng(CURRENT_ORDER.getPickupLat(),CURRENT_ORDER.getPickupLong());
        List<LatLng> points = new ArrayList<>();
        points.add(driver);
        points.add(pickup);
        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(points)
                .build();
        routing.execute();
    }

    private void populateMap() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                askLocationPermission();
                return;
            }
        }

        mMap.setMyLocationEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        addRoute();
    }

    private void askLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1001);
        }
    }

    private void checkForDistanceToSendNotification() throws JSONException {
        int percentageLeft = (int) ((int) distanceRemaining  / totalDistance * 100);
        mDriverMarker.setPosition(driver);
        NotificationPayload payload = new NotificationPayload();
        payload.setDriver_id(escapeValue(USER_ME.getUid()));
        payload.setUser_id(escapeValue(CURRENT_ORDER.getUser_id()));
        String group_id = escapeValue("--NA--");
        if(CURRENT_ORDER.getShared())
            group_id = Helper.getConcatenatedID(CURRENT_ORDER.getOrder_id(),USER_ME.getUid());
        payload.setGroup_id(escapeValue(group_id));
        payload.setTitle(escapeValue("Order Updates"));
        payload.setPercentage_left(escapeValue(""+percentageLeft));
        payload.setType(Helper.NOTI_TYPE_ORDER_WAITING);
        payload.setOrder_id(escapeValue(CURRENT_ORDER.getOrder_id()));
        String token = SELECTED_PASSENGER.getUser_token();
        if(percentageLeft < 25 && !NotificaionsDone[0]){
            NotificaionsDone[0] = true;
            payload.setDescription(escapeValue("Driver is reaching soon"));
            payload.setType(Helper.NOTI_TYPE_ORDER_WAITING_LONG);
            String str = new Gson().toJson(payload);
            try {
                JSONObject json = new JSONObject(str);
                new PushNotifictionHelper(getApplicationContext()).execute(token,json);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }else if(percentageLeft < 50 && !NotificaionsDone[1]){
            NotificaionsDone[1] = true;
            payload.setDescription(escapeValue("Driver is reaching soon"));
            String str = new Gson().toJson(payload);
            try {
                JSONObject json = new JSONObject(str);
                new PushNotifictionHelper(getApplicationContext()).execute(token,json);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }else if(percentageLeft < 75&& !NotificaionsDone[2]){
            NotificaionsDone[2] = true;
            payload.setDescription(escapeValue("Driver is reaching soon"));
            String str = new Gson().toJson(payload);
            try {
                JSONObject json = new JSONObject(str);
                new PushNotifictionHelper(getApplicationContext()).execute(token,json);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }else if(percentageLeft < 100 && !NotificaionsDone[3]){
            NotificaionsDone[3] = true;
            payload.setDescription(escapeValue("Driver is coming your way"));
            String str = new Gson().toJson(payload);
            try {
                JSONObject json = new JSONObject(str);
                new PushNotifictionHelper(getApplicationContext()).execute(token,json);
            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
    }

    private String escapeValue(String value) {
        return "\""+value+"\"";
    }

    private MarkerOptions getDesiredMarker(float kind, LatLng posToSet, String title) {
        return new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(kind))
                .position(posToSet).title(title);
    }

    public void setAnimation(GoogleMap myMap, final List<LatLng> directionPoint, final Bitmap bitmap) {
        Marker marker = myMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                .position(directionPoint.get(0))
                .flat(true));

        myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(directionPoint.get(0), 10));

        animateMarker(myMap, marker, directionPoint, false);
    }
    private void animateMarker(GoogleMap myMap, final Marker marker, final List<LatLng> directionPoint,
                               final boolean hideMarker) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = myMap.getProjection();
        final long duration = 30000;

        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            int i = 0;

            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);
                if (i < directionPoint.size())
                    marker.setPosition(directionPoint.get(i));
                i++;


                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                } else {
                    if (hideMarker) {
                        marker.setVisible(false);
                    } else {
                        marker.setVisible(true);
                    }
                }
            }
        });
    }


    public void addRoute() {
        waypoints = new ArrayList<>();
        getRoutePoints();
        start = waypoints.get(0);
        end = waypoints.get(waypoints.size() - 1);

        CameraUpdate center = CameraUpdateFactory.newLatLngZoom(start,12);
        mMap.animateCamera(center);
        PolylineOptions line = new PolylineOptions().addAll(waypoints);
        mMap.addPolyline(line);
        MarkerOptions options = new MarkerOptions();
        options.position(start);
        Bitmap pickupPin = Helper.convertToBitmap(getResources().getDrawable(R.drawable.pickup_pin),70,120);
        Bitmap dropoffPin = Helper.convertToBitmap(getResources().getDrawable(R.drawable.dropoff_pin),70,120);
        options.icon(BitmapDescriptorFactory.fromBitmap(pickupPin));
        mMap.addMarker(options);
        // End marker
        options = new MarkerOptions();
        options.position(end);
        options.icon(BitmapDescriptorFactory.fromBitmap(dropoffPin));
        mMap.addMarker(options);
    }

    private void getRoutePoints() {
        for (RoutePoints points : CURRENT_ORDER.getSELECTED_ROUTE()){
            waypoints.add(new LatLng(points.getLatitude(),points.getLongitude()));
        }
    }

    @Override
    public void onRoutingFailure(RouteException e) {
    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {

//        polylines = new ArrayList<>();
        //add route(s) to the map.

        Route shortestRoute = route.get(shortestRouteIndex);
        if (totalDistance < 0 || distanceRemaining > totalDistance)
            totalDistance = shortestRoute.getDistanceValue();
//        int colorIndex = shortestRouteIndex % COLORS.length;
//        PolylineOptions polyOptions = new PolylineOptions();
//        polyOptions.color(getResources().getColor(COLORS[colorIndex]));
//        polyOptions.width(10 + shortestRouteIndex * 3);
//        polyOptions.addAll(shortestRoute.getPoints());
//        Polyline polyline = mMap.addPolyline(polyOptions);
//        polylines.add(polyline);
        if (driver == null && myLocation != null)
            driver = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
        distanceRemaining = shortestRoute.getDistanceValue();

        try {
            if(mDriverMarker != null && driver != null && myLocation != null)
                checkForDistanceToSendNotification();
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void onRoutingCancelled() {

    }


    public void sendNotification(String title, String message) {
        //Get an instance of NotificationManager//
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(message);
        // Gets an instance of the NotificationManager service//
        NotificationManager mNotificationManager =
        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(001, mBuilder.build());
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 1001){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                populateMap();
            }
        }
        else
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    private void setupOrdersListener() {
        db_ref_order.child(CURRENT_ORDER_ID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists())
                    return;
                Order order = dataSnapshot.getValue(Order.class);
                if(order != null){
                    if(order.getStatus() == Order.OrderStatusInProgress &&
                            order.getDriver_id().equals(USER_ME.getUid())
                            && Helper.checkWithinRadius(myLocation, new LatLng(order.getPickupLat(),order.getPickupLong()))){
                        // order is within reach and it's assigned.
                        CURRENT_ORDER = order;
                        IS_RIDE_SHARED = order.getShared();
                        goDrawRoute();
                        if(IS_RIDE_SHARED){
                            fetchThatGroup();
                        }else{
                            fetchThatCustomer();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void goDrawRoute() {

    }

    private void fetchThatGroup() {
        if(CURRENT_SHARED_RIDE != null)
            return;
        db_ref_group.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                CURRENT_SHARED_RIDE  = dataSnapshot.exists() ? dataSnapshot.getValue(SharedRide.class) : null;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void fetchThatCustomer() {
        if(SELECTED_PASSENGER != null)
            showDataOnMap();
    }

    private void showDataOnMap() {

        if(mMap != null && SELECTED_PASSENGER != null){
            // show User
            pickup = new LatLng(CURRENT_ORDER.getPickupLat(), CURRENT_ORDER.getPickupLong());
            dropoff = new LatLng(CURRENT_ORDER.getDropoffLat(), CURRENT_ORDER.getDropoffLat());
            MarkerOptions options = new MarkerOptions();
            options.title("Pickup").position(pickup).icon(BitmapDescriptorFactory.fromResource(R.drawable.pickup_pin));
            mMap.addMarker(options);
            MarkerOptions options2 = new MarkerOptions();
            options2.title("Dropoff").position(dropoff).icon(BitmapDescriptorFactory.fromResource(R.drawable.dropoff_pin));
            mMap.addMarker(options2);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pickup, 12f));
            // show Driver
        }

    }


    private void updateUserLocation(){
        myLocation = LocationManagerService.mLastLocation;
        if(myLocation != null && USER_ME != null){

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mDriverMarker == null  && mMap != null) {
                        driver = new LatLng(myLocation.getLatitude(),myLocation.getLongitude());
                        MarkerOptions options = new MarkerOptions();
                        options.position(driver);
                        Bitmap driverPin = Helper.convertToBitmap(getResources().getDrawable(R.drawable.ic_option_nano), 70, 70);
                        options.icon(BitmapDescriptorFactory.fromBitmap(driverPin));
                        mDriverMarker = mMap.addMarker(options);
                    }
                }
            });

            String latitude = "latitude";
            String longitude = "longitude";
            db_ref_driver.child(USER_ME.getUid()).child(latitude).setValue(myLocation.getLatitude());
            db_ref_driver.child(USER_ME.getUid()).child(longitude).setValue(myLocation.getLongitude());
        }
    }



    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;

        populateMap();
        showDataOnMap();
    }


    private class Every10Seconds extends TimerTask{
        @Override
        public void run() {
            updateUserLocation();
            requestNewRoute();
        }
    }

}
