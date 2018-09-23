package com.logixcess.smarttaxiapplication.DriverModule;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.logixcess.smarttaxiapplication.Models.NotificationPayload;
import com.logixcess.smarttaxiapplication.Models.Order;
import com.logixcess.smarttaxiapplication.Models.RoutePoints;
import com.logixcess.smarttaxiapplication.Models.SharedRide;
import com.logixcess.smarttaxiapplication.Models.User;
import com.logixcess.smarttaxiapplication.Models.UserFareRecord;
import com.logixcess.smarttaxiapplication.R;
import com.logixcess.smarttaxiapplication.Services.LocationManagerService;
import com.logixcess.smarttaxiapplication.Utils.FareCalculation;
import com.logixcess.smarttaxiapplication.Utils.Helper;
import com.logixcess.smarttaxiapplication.Utils.PushNotifictionHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends DriverMainActivity implements OnMapReadyCallback, RoutingListener {


    
    public static final String KEY_CURRENT_SHARED_RIDE = "key_shared_ride";
    public static final String KEY_CURRENT_ORDER = "current_order";
    private GoogleMap mMap;
    private DatabaseReference db_ref_user,db_ref_drivers;
    
    private Location myLocation = null;
    private LatLng dropoff, pickup;
    private String currentOrderId = "";
    private LatLng start, end;
    private ArrayList<LatLng> waypoints;
    private Marker mDriverMarker;
    private ArrayList<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.colorPrimary, R.color.colorPrimary,R.color.colorPrimaryDark,R.color.colorAccent,R.color.primary_dark_material_light};
    
    private double totalDistance = 0, totalTime = 120; // total time in minutes
    private DatabaseReference db_ref;
    private LatLng driver = null;
    private boolean IS_ROUTE_ADDED = false;
    
    private HashMap<String, Marker> PickupMarkers;
    private CountDownTimer mCountDowntimer;
    private Marker pmarker;
    private ArrayList<LatLng> mPassengerPoints;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Helper.IS_FROM_CHILD = true;
        super.onCreate(savedInstanceState);
        firebase_db = FirebaseDatabase.getInstance();
        db_ref_order = firebase_db.getReference().child(Helper.REF_ORDERS);
        db_ref_user = firebase_db.getReference().child(Helper.REF_USERS);
        db_ref_drivers = firebase_db.getReference().child(Helper.REF_DRIVERS);
        db_ref_group = firebase_db.getReference().child(Helper.REF_GROUPS);
        userMe = FirebaseAuth.getInstance().getCurrentUser();
        Bundle bundle = getIntent().getExtras();
        if(bundle != null && bundle.containsKey(KEY_CURRENT_ORDER)){
            currentOrder = bundle.getParcelable(KEY_CURRENT_ORDER);
            if(currentOrder != null && currentOrder.getShared()){

                if(currentSharedRide != null && currentSharedRide.getGroup_id() != null) {
                
                } else if(currentOrder.getShared()){
                    fetchThatGroup();
                }

            }else if(currentOrder != null){
            
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
            db_ref.child(Helper.REF_USERS).child(currentOrder.getUser_id()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        currentUser = dataSnapshot.getValue(User.class);
                        if(currentUser != null && mMap != null) {
                            if(currentOrder.getShared()){}
//                                requestNewRoute();
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            try {
                if(currentOrder.getShared()) {
                    ordersInSharedRide = new ArrayList<>();
                    goFetchOrderById();
                }
            }catch (NullPointerException i){}
            new Timer().schedule(new Every10Seconds(),5000,10000);
        }else{
            // driver id not provided
            finish();
        }
    }

    private void requestNewRoute() {
        
        if(myLocation == null || IS_ROUTE_ADDED)
            return;
        driver = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
        if(pickup == null)
            pickup = new LatLng(currentOrder.getPickupLat(), currentOrder.getPickupLong());
        mPassengerPoints = new ArrayList<>();
        mPassengerPoints.add(driver);
        Bitmap pickupPin = Helper.convertToBitmap(getResources().getDrawable(R.drawable.pickup_pin),70,120);
        Bitmap dropoffPin = Helper.convertToBitmap(getResources().getDrawable(R.drawable.dropoff_pin),70,120);
        LatLng temp = new LatLng(currentOrder.getDropoffLat(),currentOrder.getDropoffLong());
    
        if(currentOrder.getShared()){
            for(Order order : ordersInSharedRide){
                LatLng pickup = new LatLng(order.getPickupLat(), order.getPickupLong());
                LatLng dropoff = new LatLng(order.getDropoffLat(), order.getDropoffLong());
                mPassengerPoints.add(pickup);
                if(!order.getOrder_id().equals(currentOrder.getOrder_id()))
                    mPassengerPoints.add(dropoff);
                
                
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MarkerOptions options = new MarkerOptions();
                        options.position(pickup);
                        options.icon(BitmapDescriptorFactory.fromBitmap(pickupPin));
                        options.title(order.getPickup());
                        if(PickupMarkers == null)
                            PickupMarkers = new HashMap<>();
                        if(!PickupMarkers.containsKey(order.getUser_id()))
                            PickupMarkers.put(order.getUser_id(),mMap.addMarker(options));
                        // End marker
                        options = new MarkerOptions();
                        options.position(dropoff);
                        options.icon(BitmapDescriptorFactory.fromBitmap(dropoffPin));
                        mMap.addMarker(options);
                    }
                });
                
            }
        }else{
            mPassengerPoints.add(pickup);
        }
        if(currentOrder.getShared()) {
            mPassengerPoints.add(temp);
            mPassengerPoints = sortPointsByDistance();
        }
        
        
//        Routing routing = new Routing.Builder()
//                .travelMode(AbstractRouting.TravelMode.DRIVING)
//                .withListener(this)
//                .alternativeRoutes(false)
//                .waypoints(mPassengerPoints)
//                .optimize(true)
//                .build();
//        routing.execute();
    }
    
    private ArrayList<LatLng> sortPointsByDistance() {
        List<LatLng> points = new ArrayList<>();
        int[] indexes = new int[mPassengerPoints.size()];
        for(int i = 0; i < indexes.length; i++)
            indexes[i] = i;
        double prevD = 0;
        double dist = 0;
        points.add(mPassengerPoints.get(0));
        for(int i = mPassengerPoints.size() - 1; i > 0; i--){
            dist = distance(mPassengerPoints.get(0).latitude, mPassengerPoints.get(0).longitude,
                    mPassengerPoints.get(i).latitude,
                    mPassengerPoints.get(i).longitude, 0.0, 0.0);
            if (dist > prevD) {
                prevD = dist;
                if(i == mPassengerPoints.size() - 1)
                    continue;
                int item = indexes[i];
                indexes[i] = indexes[i + 1];
                indexes[i + 1] = item;
                Log.i("Indexes ",indexes[i] + " and " + indexes[i + 1]);
            }
        }
        
        return mPassengerPoints;
        
    }
    public static double distance(double lat1, double lat2, double lon1,
                                  double lon2, double el1, double el2) {
        
        final int R = 6371; // Radius of the earth
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters
        
        double height = el1 - el2;
        
        distance = Math.pow(distance, 2) + Math.pow(height, 2);
        
        return Math.sqrt(distance);
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
        if(!currentOrder.getShared())
            addRoute();
    }

    private void askLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1001);
        }
    }

    private void checkForDistanceToSendNotification(Order currentOrder,User currentUser, double distanceRemaining )  {
//      int percentageLeft = (int) ((int) distanceRemaining  / totalDistance * 100);
        boolean[] NotificationsDone = currentOrder.getNotificaionsDone();
        mDriverMarker.setPosition(driver);
        NotificationPayload payload = new NotificationPayload();
        payload.setDriver_id(escapeValue(userMe.getUid()));
        payload.setUser_id(escapeValue(currentOrder.getUser_id()));
        String group_id = "--NA--";
        if(currentOrder.getShared())
            group_id = Helper.getConcatenatedID(currentOrder.getOrder_id(), userMe.getUid());
        payload.setGroup_id(escapeValue(group_id));
        payload.setTitle(escapeValue("Order Updates"));
        payload.setPercentage_left(escapeValue(""+distanceRemaining));
        payload.setType(Helper.NOTI_TYPE_ORDER_WAITING);
        payload.setOrder_id(escapeValue(currentOrder.getOrder_id()));
        String token = currentUser.getUser_token();
        if(distanceRemaining < 10 && NotificationsDone[0] && mCountDowntimer == null) {
            mCountDowntimer = new CountDownTimer(300000, 60000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    payload.setDescription(escapeValue("Driver is Waiting outside"));
                    payload.setType(Helper.NOTI_TYPE_ORDER_WAITING_LONG);
                    String str = new Gson().toJson(payload);
                    try {
                        JSONObject json = new JSONObject(str);
                        new PushNotifictionHelper(getApplicationContext()).execute(token,json);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
    
                }
    
                @Override
                public void onFinish() {
                    findViewById(R.id.phone_call_container).setVisibility(View.VISIBLE);
                }
            }.start();
            
        }else if(distanceRemaining < 100 && !NotificationsDone[0]){
            NotificationsDone[0] = true;
            NotificationsDone[1] = true;
            NotificationsDone[2] = true;
            NotificationsDone[3] = true;
            // TODO: change the color of driver marker.
            
            payload.setDescription(escapeValue("Driver is reaching soon"));
            payload.setType(Helper.NOTI_TYPE_ORDER_WAITING_LONG);
            String str = new Gson().toJson(payload);
            try {
                JSONObject json = new JSONObject(str);
                new PushNotifictionHelper(getApplicationContext()).execute(token,json);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }else if(distanceRemaining < 200 && !NotificationsDone[1]){
            NotificationsDone[1] = true;
            NotificationsDone[2] = true;
            NotificationsDone[3] = true;
            payload.setDescription(escapeValue("Driver is reaching soon"));
            String str = new Gson().toJson(payload);
            try {
                JSONObject json = new JSONObject(str);
                new PushNotifictionHelper(getApplicationContext()).execute(token,json);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }else if(distanceRemaining < 250 && !NotificationsDone[2]){
            NotificationsDone[2] = true;
            NotificationsDone[3] = true;
            payload.setDescription(escapeValue("Driver is reaching soon"));
            String str = new Gson().toJson(payload);
            try {
                JSONObject json = new JSONObject(str);
                new PushNotifictionHelper(getApplicationContext()).execute(token,json);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }else if(!NotificationsDone[3]){
            NotificationsDone[3] = true;
            payload.setDescription(escapeValue("Driver is coming your way"));
            String str = new Gson().toJson(payload);
            try {
                JSONObject json = new JSONObject(str);
                new PushNotifictionHelper(getApplicationContext()).execute(token,json);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            
        }
        currentOrder.setNotificaionsDone(NotificationsDone);
    }

    
    /*calculate pickup distance for notification*/
    private void calculatePickupDistance(){
        if(currentPassengers == null && currentUser != null){
            Location pickup = new Location("pickup");
            pmarker = PickupMarkers.get(currentUser.getUser_id());
            pickup.setLatitude(pmarker.getPosition().latitude);
            pickup.setLongitude(pmarker.getPosition().longitude);
            double distanceRemaining = myLocation.distanceTo(pickup);
            
//            checkForDistanceToSendNotification(currentOrder, currentUser, distanceRemaining);
        } else if(currentPassengers != null) {
            
            double distanceRemaining;
            Location pickup = new Location("pickup");
            for (User user : currentPassengers) {
                Marker marker = PickupMarkers.get(user.getUser_id());
                pickup.setLatitude(marker.getPosition().latitude);
                pickup.setLongitude(marker.getPosition().longitude);
                distanceRemaining = myLocation.distanceTo(pickup);
                int counter = 0;
                for (Order order : ordersInSharedRide) {
                    if (distanceRemaining < 10 && order.getStatus() == Order.OrderStatusWaiting) {
                        order = goUpdateOrderStatus(order);
                        ordersInSharedRide.add(counter, order);
                    }
            
                    counter++;
                    if (order.getUser_id().equals(user.getUser_id())) {
//                        checkForDistanceToSendNotification(order, user, distanceRemaining);
                
                        if (order.getStatus() == Order.OrderStatusInProgress) {
                            order.setOnRide(true);
                        } else if (order.getStatus() == Order.OrderStatusCompleted) {
                            order.setOnRide(false);
                        }
                
                
                        break;
                    }
                }
            }
    
        }
    
    }
    
    private Order goUpdateOrderStatus(Order order) {
        order.setStatus(Order.OrderStatusInProgress);
        db_ref_order.child(order.getOrder_id()).setValue(order);
        return order;
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
        options.title(currentOrder.getPickup());
        if(PickupMarkers == null)
            PickupMarkers = new HashMap<>();
        if(!PickupMarkers.containsKey(currentOrder.getUser_id()))
           PickupMarkers.put(currentOrder.getUser_id(),mMap.addMarker(options));
        // End marker
        options = new MarkerOptions();
        options.position(end);
        options.icon(BitmapDescriptorFactory.fromBitmap(dropoffPin));
        mMap.addMarker(options);
    }

    private void getRoutePoints() {
        for (RoutePoints points : currentOrder.getSelectedRoute()){
            waypoints.add(new LatLng(points.getLatitude(),points.getLongitude()));
        }
    }

    @Override
    public void onRoutingFailure(RouteException e) {
        Log.e("Err",e.getMessage() != null ? e.getMessage() : "error getting Route");
    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {

        polylines = new ArrayList<>();
        //add route(s) to the map.
        IS_ROUTE_ADDED = true;
        Route shortestRoute = route.get(shortestRouteIndex);
//        if (totalDistance < 0 || distanceRemaining > totalDistance)
            totalDistance = shortestRoute.getDistanceValue();
        int colorIndex = shortestRouteIndex % COLORS.length;
        PolylineOptions polyOptions = new PolylineOptions();
        polyOptions.color(getResources().getColor(COLORS[colorIndex]));
        polyOptions.width(10 + shortestRouteIndex * 3);
        polyOptions.addAll(shortestRoute.getPoints());
        Polyline polyline = mMap.addPolyline(polyOptions);
        polylines.add(polyline);
        if (driver == null && myLocation != null)
            driver = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
//        distanceRemaining = shortestRoute.getDistanceValue();
        if(mDriverMarker == null)
            mDriverMarker = mMap.addMarker(new FareCalculation().getVehicleMarkerOptions(MapsActivity.this, driver, currentOrder.getVehicle_id()));
        if(mDriverMarker != null && driver != null && myLocation != null){
            calculatePickupDistance();
        }
        
//            checkForDistanceToSendNotification();
    }

    @Override
    public void onRoutingCancelled() {
    
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
    
    private void fetchThatGroup() {
        if(currentSharedRide != null)
            return;
        String groupId = Helper.getConcatenatedID(currentOrder.getOrder_id(), userMe.getUid());
        db_ref_group.child(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                currentSharedRide = dataSnapshot.exists() ? dataSnapshot.getValue(SharedRide.class) : null;
                if(currentSharedRide == null || currentSharedRide.getGroup_id() == null){
                    Toast.makeText(MapsActivity.this, "Something went Wrong.", Toast.LENGTH_SHORT).show();
                    finish();
                }else{
                    orderIDs = currentSharedRide.getOrderIDs();
                    
                    goGetOrdersForGroup();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    
    private void showDataOnMap() {

        if(mMap != null && currentUser != null){
            // show User
            pickup = new LatLng(currentOrder.getPickupLat(), currentOrder.getPickupLong());
            dropoff = new LatLng(currentOrder.getDropoffLat(), currentOrder.getDropoffLat());
            MarkerOptions options = new MarkerOptions();
            options.title("Pickup").position(pickup).icon(BitmapDescriptorFactory.fromResource(R.drawable.pickup_pin));
            pmarker = mMap.addMarker(options);
            MarkerOptions options2 = new MarkerOptions();
            options2.title("Dropoff").position(dropoff).icon(BitmapDescriptorFactory.fromResource(R.drawable.dropoff_pin));
            mMap.addMarker(options2);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pickup, 12f));
            // show Driver
        }

    }


    private void updateUserLocation(){
        myLocation = LocationManagerService.mLastLocation;
        if(myLocation != null && userMe != null){

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mDriverMarker == null  && mMap != null) {
                        driver = new LatLng(myLocation.getLatitude(),myLocation.getLongitude());
                        mDriverMarker = mMap.addMarker(new FareCalculation().getVehicleMarkerOptions(MapsActivity.this, driver, currentOrder.getVehicle_id()));
                    }
                }
            });

            String latitude = "latitude";
            String longitude = "longitude";
            double lat = Helper.roundOffDouble(myLocation.getLatitude());
            double lng = Helper.roundOffDouble(myLocation.getLongitude());
            db_ref_drivers.child(userMe.getUid()).child(latitude).setValue(lat);
            db_ref_drivers.child(userMe.getUid()).child(longitude).setValue(lng);
        }
    }



    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;
        populateMap();
        showDataOnMap();
    }
    
    public void callCurrentPassenger(View view) {
        String phone = getPassengerPhoneNumber();
        if(phone != null && !phone.isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone));
            startActivity(intent);
        } else{
            Toast.makeText(this, "Phone number is not correct", Toast.LENGTH_SHORT).show();
        }
        
    }
    
    private String getPassengerPhoneNumber() {
        for(User u : currentPassengers)
            if(u.getUser_id().equals(currentOrder.getUser_id()))
                return u.getPhone();
        
        return null;
    }
    
    
    private class Every10Seconds extends TimerTask{
        @Override
        public void run() {
            updateUserLocation();
            if(!IS_ROUTE_ADDED ) {
                if(!currentOrder.getShared())
                    requestNewRoute();
                else{
                    if(ordersInSharedRide.size() > 0)
                        requestNewRoute();
                }
            }
            else {
                try {
                    if(mDriverMarker != null && driver != null && myLocation != null) {
                        Location location = new Location("pickup");
                        location.setLatitude(start.latitude);
                        location.setLongitude(start.longitude);
                        if(totalDistance == 0)
                            totalDistance = myLocation.distanceTo(location);
//                        distanceRemaining = myLocation.distanceTo(location);
                        driver = new LatLng(myLocation.getLatitude(),myLocation.getLongitude());
//                        if(distanceRemaining > totalDistance)
//                            return;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                calculatePickupDistance();
                            }
                        });

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            runtimeFareCalculation();
        }
    }
    
    
    private void initNextOrderVars(){
        totalDistance = 0;
        totalTime = 0;
        findViewById(R.id.phone_call_container).setVisibility(View.GONE);
        if(mCountDowntimer != null)
            mCountDowntimer.cancel();
        mCountDowntimer = null;
    }
    

    
    
    
    
    private void getTheNextNearestDropOff(){
        double totalDistance = 0;
        boolean isAllOrdersCompleted = true;
        if(currentSharedRide == null)
            return;
        else{
            for (Order order : ordersInSharedRide){
                if(order.getStatus() == Order.OrderStatusInProgress){
                    if(currentOrder != null && currentOrder.getOrder_id().equals(order.getOrder_id())){
                        // current order is in progress.
                        return;
                    }
                    Location dropOff = new Location("dropoff");
                    dropOff.setLatitude(order.getDropoffLat());
                    dropOff.setLongitude(order.getDropoffLong());
                    isAllOrdersCompleted = false;
                    
                    if(myLocation == null)
                        continue;
                    
                    if(totalDistance == 0) {
                        totalDistance = myLocation.distanceTo(dropOff);
                        currentOrder = order;
                    }else if(myLocation.distanceTo(dropOff) < totalDistance ) {
                        totalDistance = myLocation.distanceTo(dropOff);
                        currentOrder = order;
                    }
                }
            }
        }
        
        if(isAllOrdersCompleted){
            DatabaseReference db_ref_order_to_driver;
            db_ref_order_to_driver = firebase_db.getReference().child(Helper.REF_ORDER_TO_DRIVER);
            db_ref_order_to_driver.child(userMe.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Toast.makeText(MapsActivity.this, "Your Order has been Completed", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void goFetchOrderById(){
        
        orderIDs = currentSharedRide.getOrderIDs();
        if(currentSharedRide.getGroup_id() == null) {
            fetchThatGroup();
            return;
        }
        goGetOrdersForGroup();
    }
    private void goGetOrdersForGroup() {
        for (Map.Entry<String, Boolean> entry : orderIDs.entrySet()) {
            String key = entry.getKey();
            db_ref_order.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(!dataSnapshot.exists())
                        return;
                    Order order = dataSnapshot.getValue(Order.class);
                    if(order != null){
                        if((order.getStatus() == Order.OrderStatusWaiting
                                || order.getStatus() == Order.OrderStatusInProgress) &&
                                order.getDriver_id().equals(userMe.getUid())){
                            if(!checkIfOrderExists(order.getOrder_id(), ordersInSharedRide))
                                ordersInSharedRide.add(order);
                        }
                    }
                    if(orderIDs.size() == ordersInSharedRide.size()) {
                        addMarkersForNewRiders();
                        addOrdersListener();
                        requestNewRoute();
    
                    }
                }
            
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                
                }
            });
        }
    }
    
    private boolean checkIfOrderExists(String key, List<Order> ordersInSharedRide) {
        for(Order order : ordersInSharedRide)
            if(order.getOrder_id().equals(key))
                return true;
        return false;
    }
    
    private void addOrdersListener() throws NullPointerException{
        db_ref_order.child(currentOrderId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(!dataSnapshot.exists())
                    return;
                Order order = dataSnapshot.getValue(Order.class);
                if(order != null){
                    if(order.getStatus() == Order.OrderStatusInProgress &&
                            order.getDriver_id().equals(userMe.getUid()) && order.getShared()){
                        getTheNextNearestDropOff();
                        fetchThatGroup();
                    }else {
                        // order is single
//                        markOrderComplete();
                    }
                }
            }
            
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(!dataSnapshot.exists())
                    return;
                Order order = dataSnapshot.getValue(Order.class);
                if(order != null){
                    if(currentOrder.getShared()){
                        updateOrderLocally(order);

//                        if(currentOrder.getOrder_id().equalsIgnoreCase(order.getOrder_id())
//                                && order.getDriver_id().equals(userMe.getUid())) {
//                            if (order.getStatus() == Order.OrderStatusInProgress) {
//
//                            }else if (order.getStatus() == Order.OrderStatusCompleted){
//                                updateOrderLocally(order);
//                                initNextOrderVars();
//                                getTheNextNearestDropOff();
//                            }else if (order.getStatus() == Order.OrderStatusPending){
//
//                            }
//                        }
                    }
                    else {
                        if(currentOrder.getOrder_id().equals(order.getOrder_id())
                                && order.getDriver_id().equals(userMe.getUid())) {
                            if (order.getStatus() == Order.OrderStatusInProgress) {
                            }else if (order.getStatus() == Order.OrderStatusCompleted){
                                markOrderComplete();
                            }else if (order.getStatus() == Order.OrderStatusPending){

                            }
                        }
                    }
                }


            }


            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
       

    }
    
    private void updateOrderLocally(Order order) {
        try {
            int index = 0;
            boolean isToRefresh = false;
            for (Order o : ordersInSharedRide) {
                if (o.getOrder_id().equals(order.getOrder_id())) {
                    if (currentOrder.getOrder_id().equalsIgnoreCase(order.getOrder_id())
                            && order.getDriver_id().equals(userMe.getUid())) {
                        if (order.getStatus() == Order.OrderStatusInProgress) {
                            isToRefresh = true;
                        } else if (order.getStatus() == Order.OrderStatusCompleted) {
                            isToRefresh = true;
                        } else if (order.getStatus() == Order.OrderStatusPending) {
                            isToRefresh = true;
                        }
                    }
                    ordersInSharedRide.add(index, order);
                }
                index++;
            }
            if (isToRefresh) {
                initNextOrderVars();
                getTheNextNearestDropOff();
            }
        }catch (ConcurrentModificationException ignore){}
        
    }
    
    private void markOrderComplete() {
        DatabaseReference db_ref_order_to_driver;
        db_ref_order_to_driver = firebase_db.getReference().child(Helper.REF_ORDER_TO_DRIVER);
        db_ref_order_to_driver.child(userMe.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(MapsActivity.this, "Your Order has been Completed", Toast.LENGTH_SHORT).show();
            }
        });
        finish();
    }
    
    private void addMarkersForNewRiders(){
        if(PickupMarkers == null) {
            PickupMarkers = new HashMap<>();
        }
        if(currentPassengers == null)
            currentPassengers = new ArrayList<>();
        if(ordersInSharedRide != null){
            for(Order order : ordersInSharedRide){
                if(!PickupMarkers.containsKey(order.getUser_id())){
                    Bitmap pickupPin = Helper.convertToBitmap(getResources().getDrawable(R.drawable.pickup_pin),70,120);
                    MarkerOptions options = new MarkerOptions().
                            icon(BitmapDescriptorFactory.fromBitmap(pickupPin))
                            .title(order.getPickup()).position(new LatLng(order.getPickupLat(),order.getPickupLong()));
                    PickupMarkers.put(order.getUser_id(),mMap.addMarker(options));
                    goGetUserById(order.getUser_id());
                }
            }
        }
    }
    
    private void goGetUserById(String user_id) {
        db_ref_user.child(user_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    User user = dataSnapshot.getValue(User.class);
                    if(user != null){
                        if(!currentPassengers.contains(user))
                            currentPassengers.add(user);
                    }
                }
            }
    
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
        
            }
        });
    }
    
    
    /*
    *
    *
    * How route thing will work
    *  8 points route
    *
    *
    * */
    
    
    
    
  /*
    Shared Ride Fare Calculation
    */
    private void runtimeFareCalculation(){
        if(currentSharedRide != null) {
            // current ride is shared
            if(myLocation.getLatitude() == 0 && myLocation.getLongitude() == 0)
                return;
            currentSharedRide = mFareCalc.calculateFareForSharedRide(ordersInSharedRide, currentSharedRide, myLocation, currentOrder.getVehicle_id());
            for (Map.Entry<String, UserFareRecord> entry : currentSharedRide.getPassengerFares().entrySet()) {
                String key = entry.getKey();
                UserFareRecord fareRecord = currentSharedRide.getPassengerFares().get(key);
                if(fareRecord != null && fareRecord.getUserFare() != null) {
                    Log.i("FareCalculation",fareRecord.getUserFare().toString());
                    Log.i("FareCalculation", "Count: " + fareRecord.getUserFare().size());
                }
            }
            db_ref_group.child(currentSharedRide.getGroup_id()).setValue(currentSharedRide);
        }else{
        
        }
    }
    
    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(locationBroadcastReceiver);
    }
    
    @Override
    public void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver(locationBroadcastReceiver, new IntentFilter(Helper.BROADCAST_LOCATION));
    }
    
    BroadcastReceiver locationBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            myLocation = LocationManagerService.mLastLocation;
            if(myLocation != null && mDriverMarker != null){
                mDriverMarker.setPosition(new LatLng(myLocation.getLatitude(),myLocation.getLongitude()));
                Log.i("DriverLocation",myLocation.toString());
            }
        }
    };
    
    
    
}
