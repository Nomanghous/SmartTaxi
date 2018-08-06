package com.logixcess.smarttaxiapplication.CustomerModule;

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
import com.logixcess.smarttaxiapplication.Models.Driver;
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

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, RoutingListener {

    public static final String KEY_CURRENT_SHARED_RIDE = "key_shared_ride";
    private GoogleMap mMap;
    private Order CURRENT_ORDER = null;
    private User CURRENT_USER = null;
    private boolean IS_RIDE_SHARED = false;
    private String CUSTOMER_USER_ID = "";
    private FirebaseDatabase firebase_db;
    private DatabaseReference db_ref_order;
    private DatabaseReference db_ref_user;
    private DatabaseReference db_ref_group;
    private FirebaseUser USER_ME;
    private Location MY_LOCATION = null;
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
    private Passenger SELECTED_PASSENGER;
    private Driver SELECTED_DRIVER;
    private LatLng driver = null;
    private Marker driverMarker;
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

        Bundle bundle = getIntent().getExtras();
        if(bundle != null && bundle.containsKey(KEY_CURRENT_ORDER)){
            CURRENT_ORDER = bundle.getParcelable(KEY_CURRENT_ORDER);


            if(CURRENT_ORDER != null && CURRENT_ORDER.getShared()){
                if(bundle.containsKey(KEY_CURRENT_SHARED_RIDE)) {
                    CURRENT_SHARED_RIDE = bundle.getParcelable(KEY_CURRENT_SHARED_RIDE);
                    IS_RIDE_SHARED = true;
                }
            }else {
                finish();
                return;
            }

            askLocationPermission();
            setContentView(R.layout.activity_maps_customer);
            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
            db_ref = FirebaseDatabase.getInstance().getReference();
            db_ref_driver = db_ref.child(Helper.REF_DRIVERS).child(CURRENT_ORDER.getUser_id());
            db_ref_driver.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                       // SELECTED_PASSENGER = dataSnapshot.getValue(Passenger.class);
                        SELECTED_DRIVER = dataSnapshot.getValue(Driver.class);
                        if(SELECTED_DRIVER != null && mMap != null) {
                            MY_LOCATION.setLatitude(SELECTED_DRIVER.getLatitude());
                            MY_LOCATION.setLongitude(SELECTED_DRIVER.getLongitude());
                            requestNewRoute();
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }else{
            // driver id not provided
            finish();
        }
    }

    private void requestNewRoute() {
        driver = new LatLng(MY_LOCATION.getLatitude(), MY_LOCATION.getLongitude());
        if(pickup == null)
            pickup = new LatLng(CURRENT_ORDER.getPickupLat(),CURRENT_ORDER.getPickupLong());
        List<LatLng> points = new ArrayList<>();
        points.add(driver);
        points.add(pickup);
        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(true)
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

        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        addRoute();
    }

    private void askLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1001);
        }
    }

    private void checkForDistanceToSendNotification() throws JSONException {
        int percentageLeft = (int) ((int) distanceRemaining  / totalDistance * 100);
        driverMarker.setPosition(driver);
        NotificationPayload payload = new NotificationPayload();
        payload.setDriver_id(USER_ME.getUid());
        payload.setUser_id(CURRENT_ORDER.getUser_id());
        String group_id = "--NA--";
        if(CURRENT_ORDER.getShared())
            group_id = Helper.getConcatenatedID(CURRENT_ORDER.getOrder_id(),USER_ME.getUid());
        payload.setGroup_id(group_id);
        payload.setOrder_id(CURRENT_ORDER.getOrder_id());
        if(percentageLeft < 25){
            payload.setPercentage_left("25");
            new PushNotifictionHelper(this).execute(CURRENT_ORDER.getUser_id(),new JSONObject(new Gson().toJson(payload)));
        }else if(percentageLeft < 50){
            payload.setPercentage_left("50");
            new PushNotifictionHelper(this).execute(CURRENT_ORDER.getUser_id(),new JSONObject(new Gson().toJson(payload)));
        }else if(percentageLeft < 75){
            payload.setPercentage_left("75");
            new PushNotifictionHelper(this).execute(CURRENT_ORDER.getUser_id(),new JSONObject(new Gson().toJson(payload)));
        }else if(percentageLeft < 100){
            payload.setPercentage_left("100");
        }
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
        mDriverMarker = mMap.addMarker(getDesiredMarker(BitmapDescriptorFactory.HUE_YELLOW,start,"driver"));
        MarkerOptions options = new MarkerOptions();
        options.position(start);
        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        mMap.addMarker(options);

        // End marker
        options = new MarkerOptions();
        options.position(end);
        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        mMap.addMarker(options);




    }

    private void getRoutePoints() {
        for (RoutePoints points : CURRENT_ORDER.getSELECTED_ROUTE()){
            waypoints.add(new LatLng(points.getLatitude(),points.getLongitude()));
        }
    }

    @Override
    public void onRoutingFailure(RouteException e) {
        Toast.makeText(this, "failed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {

//        polylines = new ArrayList<>();
        //add route(s) to the map.

        Route shortestRoute = route.get(shortestRouteIndex);
        if(totalDistance < 0 || distanceRemaining >= totalDistance)
            totalDistance = shortestRoute.getDistanceValue();
//        int colorIndex = shortestRouteIndex % COLORS.length;
//        PolylineOptions polyOptions = new PolylineOptions();
//        polyOptions.color(getResources().getColor(COLORS[colorIndex]));
//        polyOptions.width(10 + shortestRouteIndex * 3);
//        polyOptions.addAll(shortestRoute.getPoints());
//        Polyline polyline = mMap.addPolyline(polyOptions);
//        polylines.add(polyline);
        distanceRemaining = shortestRoute.getDistanceValue();
        MarkerOptions options = new MarkerOptions();
        options.position(driver);
        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_car_black));
        if(mDriverMarker != null)
            mDriverMarker.remove();
        mDriverMarker = mMap.addMarker(options);
        Toast.makeText(getApplicationContext(),"Destination Arrived! ",Toast.LENGTH_SHORT).show();
//        try {
//
//            checkForDistanceToSendNotification();
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }


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
                            && Helper.checkWithinRadius(MY_LOCATION, new LatLng(order.getPickupLat(),order.getPickupLong()))){
                        // order is within reach and it's assigned.
                        CURRENT_ORDER = order;
                        IS_RIDE_SHARED = order.getShared();
                        CUSTOMER_USER_ID = order.getUser_id();
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
        db_ref_group.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    CURRENT_USER = dataSnapshot.getValue(User.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void fetchThatCustomer() {
        db_ref_user.child(CUSTOMER_USER_ID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    CURRENT_USER = dataSnapshot.getValue(User.class);
                    if(CURRENT_USER != null){
                        showDataOnMap();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showDataOnMap() {
        if(mMap != null && CURRENT_USER != null){
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
        MY_LOCATION = LocationManagerService.mLastLocation;
        if(MY_LOCATION != null && USER_ME != null){
            String latitude = "latitude";
            String longitude = "longitude";
            db_ref_user.child(USER_ME.getUid()).child(latitude).setValue(MY_LOCATION.getLatitude());
            db_ref_user.child(USER_ME.getUid()).child(longitude).setValue(MY_LOCATION.getLongitude());
        }
    }

    private void setupOrderOnMap(){
        if(CURRENT_ORDER != null){
            MarkerOptions userMarker = new MarkerOptions();

        }
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                //MY_LOCATION = location;
                //if(driver == null && SELECTED_DRIVER != null)
                  //  requestNewRoute();
            }
        });

        populateMap();
        showDataOnMap();
    }



}
